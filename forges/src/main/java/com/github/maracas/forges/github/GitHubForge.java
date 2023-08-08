package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class GitHubForge implements Forge {
	private final GitHubClientsFetcher clientsFetcher;
	private final LoadingCache<String, GHRepository> repositoryCache;

	private static final String BREAKBOT_FILE = ".github/breakbot.yml";

	private static final Logger logger = LogManager.getLogger(GitHubForge.class);

	public GitHubForge(GitHub gh, GitHubClientsFetcher clientsFetcher) {
		this.clientsFetcher = Objects.requireNonNull(clientsFetcher);
		this.repositoryCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(Duration.ofHours(1))
			.build(new CacheLoader<>() {
				@Override
				public @Nonnull GHRepository load(@Nonnull String fullName) throws IOException {
					return gh.getRepository(fullName);
				}
			});

		if (gh.isAnonymous())
			logger.warn("Unauthenticated access to GitHub APIs; likely to hit rate limit soon");
	}

	public GitHubForge(GitHub gh) {
		this(gh, new GitHubClientsScraper(Duration.ofDays(7)));
	}

	@Override
	public Repository fetchRepository(String owner, String name) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);

		GHRepository repo = fetchAndCacheRepository(owner, name);
		return new Repository(
			repo.getOwnerName(),
			repo.getName(),
			repo.getHttpTransportUrl(),
			repo.getDefaultBranch()
		);
	}

	@Override
	public Repository fetchRepository(String owner, String name, String branch) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);
		Objects.requireNonNull(branch);
		String fullName = owner + "/" + name;

		try {
			GHRepository repo = fetchAndCacheRepository(owner, name);
			GHBranch b = repo.getBranch(branch);

			return new Repository(
				repo.getOwnerName(),
				repo.getName(),
				repo.getHttpTransportUrl(),
				b.getName()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch repository %s on branch %s".formatted(fullName, branch), e);
		}
	}

	@Override
	public PullRequest fetchPullRequest(Repository repository, int number) {
		Objects.requireNonNull(repository);
		if (number < 0)
			throw new IllegalArgumentException("number < 0");

		try {
			GHRepository repo = fetchAndCacheRepository(repository.owner(), repository.name());
			GHPullRequest pr = repo.getPullRequest(number);
			GHCompare compare = repo.getCompare(pr.getBase().getCommit(), pr.getHead().getCommit());

			Commit base = new Commit(repository, pr.getBase().getSha());
			Commit head = new Commit(repository, pr.getHead().getSha());
			Commit mergeBase = new Commit(repository, compare.getMergeBaseCommit().getSHA1());
			List<GHPullRequestFileDetail> changedFiles = pr.listFiles().toList();

			return new PullRequest(
				repository,
				pr.getNumber(),
				base,
				head,
				mergeBase,
				pr.getBase().getRef(),
				pr.getHead().getRef(),
				changedFiles.stream().map(GHPullRequestFileDetail::getFilename).map(Path::of).toList()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch PR %d from repository %s".formatted(number, repository.fullName()), e);
		}
	}

	@Override
	public Commit fetchCommit(Repository repository, String sha) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(sha);

		try {
			GHRepository repo = fetchAndCacheRepository(repository.owner(), repository.name());
			GHCommit commit = repo.getCommit(sha);

			return new Commit(
				repository,
				commit.getSHA1()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch commit %s from repository %s".formatted(sha, repository.fullName()), e);
		}
	}

	@Override
	public List<Repository> fetchTopStarredClients(RepositoryModule module, int limit, int minStars) {
		Objects.requireNonNull(module);
		if (limit < 1)
			throw new IllegalArgumentException("limit < 1");

		// If we're on a fork, we retrieve the clients from the original repository
		Repository sourceRepository = getSourceRepository(module.repository());
		RepositoryModule sourceModule = new RepositoryModule(sourceRepository, module.id(), module.url());

		GitHubClientsFetcher.ClientFilter filter = client ->
			// Kinda harsh, but there are too many "unofficial" forks
			!client.name().equals(module.repository().name()) &&
			client.stars() >= minStars &&
			isValidClient(client);

		List<GitHubClient> clients = clientsFetcher.fetchClients(sourceModule, filter, limit);
		return clients.stream()
			.sorted(Comparator.comparingInt(GitHubClient::stars).reversed())
			.map(client -> {
				try {
					return Optional.of(fetchRepository(client.owner(), client.name()));
				} catch (ForgeException e) {
					return Optional.<Repository>empty();
				}
			})
			.flatMap(Optional::stream)
			.toList();
	}

	@Override
	public List<Repository> fetchAllClients(RepositoryModule module, int limit, int minStars) {
		return Stream.concat(
			fetchCustomClients(module).stream(),
			fetchTopStarredClients(module, limit, minStars).stream()
		).toList();
	}

	@Override
	public List<RepositoryModule> fetchModules(Repository repository) {
		return clientsFetcher.fetchModules(repository);
	}

	@Override
	public BreakbotConfig fetchBreakbotConfig(Repository repository) {
		Objects.requireNonNull(repository);

		try (InputStream configIn = fetchAndCacheRepository(repository.owner(), repository.name()).getFileContent(BREAKBOT_FILE).read()) {
			return BreakbotConfig.fromYaml(configIn);
		} catch (IOException e) {
			logger.error("Couldn't read .breakbot.yml from {}", repository::fullName);
			return BreakbotConfig.defaultConfig();
		}
	}

	private GHRepository fetchAndCacheRepository(String owner, String name) {
		try {
			return repositoryCache.get(owner + "/" + name);
		} catch (ExecutionException e) {
			throw new ForgeException("Couldn't fetch repository %s/%s".formatted(owner, name), e.getCause());
		}
	}

	private List<Repository> fetchCustomClients(RepositoryModule module) {
		Objects.requireNonNull(module);

		return fetchBreakbotConfig(module.repository()).clients().repositories()
			.stream()
			.map(this::getRepositoryFromBreakbot)
			.flatMap(Optional::stream)
			.toList();
	}

	private Optional<Repository> getRepositoryFromBreakbot(BreakbotConfig.GitHubRepository repo) {
		List<String> fields = Splitter.on("/").splitToList(repo.repository());

		if (fields.size() == 2) {
			String clientOwner = fields.get(0);
			String clientName = fields.get(1);

			try {
				return Optional.of(StringUtils.isEmpty(repo.branch())
					? fetchRepository(clientOwner, clientName)
					: fetchRepository(clientOwner, clientName, repo.branch())
				);
			} catch (ForgeException e) {
				// swallow this one
			}
		}

		return Optional.empty();
	}

	private boolean isValidClient(GitHubClient client) {
		try {
			GHRepository candidate = fetchAndCacheRepository(client.owner(), client.name());
			return
				!candidate.isFork() &&
				!candidate.isArchived() &&
				!candidate.isDisabled() &&
				!candidate.isPrivate();
		} catch (ForgeException e) {
			return false;
		}
	}

	private Repository getSourceRepository(Repository repository) {
		try {
			GHRepository repo = fetchAndCacheRepository(repository.owner(), repository.name());
			if (repo.getSource() != null) {
				return fetchRepository(repo.getSource().getOwnerName(), repo.getSource().getName());
			}
		} catch (IOException | ForgeException e) {
			// doesn't matter, swallow
		}

		return repository;
	}
}

