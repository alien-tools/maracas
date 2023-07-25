package com.github.maracas.forges.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GitHubForge implements Forge {
	private final GitHub gh;
	private final GitHubClientsFetcher clientsFetcher;
	private Path clientsCacheDirectory;
	private int clientsCacheExpirationDays = 1;
	private static final String BREAKBOT_FILE = ".github/breakbot.yml";

	private static final Logger logger = LogManager.getLogger(GitHubForge.class);

	public GitHubForge(GitHub gh, GitHubClientsFetcher clientsFetcher) {
		this.gh = Objects.requireNonNull(gh);
		this.clientsFetcher = Objects.requireNonNull(clientsFetcher);

		if (gh.isAnonymous())
			logger.warn("Unauthenticated access to GitHub APIs; likely to hit rate limit soon");

		Path dir;
		try {
			dir = Files.createTempDirectory("clients").toAbsolutePath();
		} catch (IOException e) {
			dir = Path.of("./clients");
		}
		this.clientsCacheDirectory = dir;
	}

	public GitHubForge(GitHub gh) {
		this(gh, new GitHubClientsFetcher());
	}

	@Override
	public Repository fetchRepository(String owner, String name) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);
		String fullName = owner + "/" + name;

		try {
			GHRepository repo = gh.getRepository(fullName);
			return new Repository(
				repo.getOwnerName(),
				repo.getName(),
				repo.getHttpTransportUrl(),
				repo.getDefaultBranch()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch repository " + fullName, e);
		}
	}

	@Override
	public Repository fetchRepository(String owner, String name, String branch) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);
		Objects.requireNonNull(branch);
		String fullName = owner + "/" + name;

		try {
			GHRepository repo = gh.getRepository(fullName);
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
			GHRepository repo = gh.getRepository(repository.fullName());
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
			GHCommit commit = gh.getRepository(repository.fullName()).getCommit(sha);

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

		// If 'repository' is a fork, we retrieve the clients from the original repository
		Repository sourceRepository = getSourceRepository(module.repository());
		RepositoryModule sourceModule = new RepositoryModule(sourceRepository, module.id(), module.url());

		List<GitHubClient> clients = fetchClients(sourceModule, limit);
		return clients.stream()
			.sorted(Comparator.comparingInt(GitHubClient::stars).reversed())
			.filter(client ->
				// Kinda harsh, but there are too many "unofficial" forks
				!client.name().equals(module.repository().name()) &&
				client.stars() >= minStars &&
				isValidClient(client)
			)
			.map(client -> fetchRepository(client.owner(), client.name()))
			.limit(limit > 0 ? limit : clients.size())
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

		try (InputStream configIn = gh.getRepository(repository.fullName()).getFileContent(BREAKBOT_FILE).read()) {
			return BreakbotConfig.fromYaml(configIn);
		} catch (IOException e) {
			logger.error("Couldn't read .breakbot.yml from {}", repository.fullName());
			return BreakbotConfig.defaultConfig();
		}
	}

	public List<Repository> fetchCustomClients(RepositoryModule module) {
		Objects.requireNonNull(module);

		return fetchBreakbotConfig(module.repository()).clients().repositories()
			.stream()
			.map(c -> {
				List<String> fields = Splitter.on("/").splitToList(c.repository());
				String clientOwner = fields.get(0);
				String clientName = fields.get(1);

				return
					StringUtils.isEmpty(c.branch())
						? fetchRepository(clientOwner, clientName)
						: fetchRepository(clientOwner, clientName, c.branch());
			})
			.toList();
	}

	private List<GitHubClient> fetchClients(RepositoryModule module, int limit) {
		if (hasClientsCache(module)) {
			return readClientsCache(module);
		}

		Stopwatch sw = Stopwatch.createStarted();

		// FIXME: dirty, but we don't know how many "raw" clients we should get
		// to reach our objectives in terms of "usable" clients with required stars
		int rawClientsToFetch = Math.max(1000, limit);
		List<GitHubClient> clients = clientsFetcher.fetchClients(module, rawClientsToFetch);
		logger.info("Fetched {} total clients for {} in {}s",
				clients.size(), module, sw.elapsed().toSeconds());

		writeClientsCache(module, clients);
		return clients;
	}

	public void setClientsCacheExpirationDays(int clientsCacheExpirationDays) {
		if (clientsCacheExpirationDays < 0)
			throw new IllegalArgumentException("clientsCacheExpirationDays < 0");
		this.clientsCacheExpirationDays = clientsCacheExpirationDays;
	}

	public void setClientsCacheDirectory(Path dir) {
		if (dir == null || !Files.exists(dir))
			throw new IllegalArgumentException("dir does not exist");

		this.clientsCacheDirectory = dir;
	}

	private boolean hasClientsCache(RepositoryModule module) {
		Path cacheFile = clientsCacheFile(module);
		if (Files.exists(cacheFile)) {
			Date modified = new Date(cacheFile.toFile().lastModified());
			Date now = Date.from(Instant.now());

			long daysDiff = TimeUnit.DAYS.convert(Math.abs(now.getTime() - modified.getTime()), TimeUnit.MILLISECONDS);
			return daysDiff <= clientsCacheExpirationDays;
		}

		return false;
	}

	private List<GitHubClient> readClientsCache(RepositoryModule module) {
		try {
			Path cacheFile = clientsCacheFile(module);
			List<GitHubClient> clients = new ObjectMapper().readValue(cacheFile.toFile(), new TypeReference<>(){});
			logger.info("Retrieved {} total clients from {}", clients.size(), cacheFile);
			return clients;
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private void writeClientsCache(RepositoryModule module, List<GitHubClient> clients) {
		try {
			Path cacheFile = clientsCacheFile(module);
			Files.createDirectories(cacheFile.getParent());
			new ObjectMapper().writeValue(cacheFile.toFile(), clients);
			logger.info("Serialized clients for {} in {}", module, cacheFile);
		} catch (IOException e) {
			logger.error("Couldn't save clients cache for %s".formatted(module), e);
		}
	}

	private boolean isValidClient(GitHubClient client) {
		try {
			GHRepository candidate = gh.getRepository(String.format("%s/%s", client.owner(), client.name()));
			return !candidate.isFork() && !candidate.isArchived() && !candidate.isDisabled();
		} catch (IOException e) {
			return false;
		}
	}

	private Repository getSourceRepository(Repository repository) {
		try {
			GHRepository repo = gh.getRepository(String.format("%s/%s", repository.owner(), repository.name()));
			if (repo != null && repo.getSource() != null) {
				return fetchRepository(repo.getSource().getOwnerName(), repo.getSource().getName());
			}
		} catch (IOException e) {
			// doesn't matter, swallow
		}

		return repository;
	}

	private Path clientsCacheFile(RepositoryModule module) {
		return clientsCacheDirectory
			.resolve(module.repository().owner())
			.resolve(module.repository().name())
			.resolve(module.id() + "-clients.json")
			.toAbsolutePath();
	}
}
