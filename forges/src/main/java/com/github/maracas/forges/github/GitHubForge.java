package com.github.maracas.forges.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GitHubForge implements Forge {
	private final GitHub gh;
	private Path clientsCacheDirectory;
	private int clientsCacheExpirationDays = 1;
	private static final String BREAKBOT_FILE = ".github/breakbot.yml";

	private static final Logger logger = LogManager.getLogger(GitHubForge.class);

	public GitHubForge(GitHub gh) {
		this.gh = Objects.requireNonNull(gh);

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
	public List<Repository> fetchTopStarredClients(Repository repository, String moduleId, int limit, int minStars) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(moduleId);

		// Beware: if 'repository' is a fork, we retrieve the clients from the original repository
		Repository actualRepository = repository;
		try {
			GHRepository repo = gh.getRepository(String.format("%s/%s", repository.owner(), repository.name()));
			if (repo != null && repo.getParent() != null) {
				actualRepository = fetchRepository(repo.getParent().getOwnerName(), repo.getParent().getName());
			}
		} catch (IOException e) {
			logger.error(e);
		}

		List<GitHubClient> clients = fetchClients(actualRepository, moduleId, limit);
		return clients
			.stream()
			.sorted(Comparator.comparingInt(GitHubClient::stars).reversed())
			.filter(client ->
				!client.name().equals(repository.name()) && // Kinda harsh, but there are too many "unofficial" forks
				client.stars() >= minStars &&
				isValidClient(client) // No fork, archived, or disabled repository
			)
			.limit(limit > 0 ? limit : clients.size())
			.map(client -> fetchRepository(client.owner(), client.name()))
			.toList();
	}

	@Override
	public List<Repository> fetchCustomClients(Repository repository) {
		Objects.requireNonNull(repository);

		return fetchBreakbotConfig(repository).clients().repositories()
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

	@Override
	public List<Repository> fetchAllClients(Repository repository, String moduleId, int limit, int minStars) {
		return Stream.concat(
			fetchCustomClients(repository).stream(),
			fetchTopStarredClients(repository, moduleId, limit, minStars).stream()
		).toList();
	}

	@Override
	public List<GitHubModule> fetchModules(Repository repository) {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		return fetcher.fetchModules();
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

	private List<GitHubClient> fetchClients(Repository repository, String moduleId, int limit) {
		Path cacheFile = clientsCacheFile(repository, moduleId);
		ObjectMapper objectMapper = new ObjectMapper();

		if (clientsCacheIsValid(cacheFile)) {
			try {
				List<GitHubClient> clients = objectMapper.readValue(cacheFile.toFile(), new TypeReference<>(){});
				logger.info("Fetched {} total clients for {} [module: {}] from {}",
						clients.size(), repository, moduleId, cacheFile);
				return clients;
			} catch (IOException e) {
				logger.error(e);
			}
		}

		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		// FIXME: dirty, but we don't know how many "raw" clients we should get
		// to reach our objectives in terms of "usable" clients with required stars
		List<GitHubClient> clients = fetcher.fetchClients(moduleId, Math.max(1000, limit));
		logger.info("Fetched {} total clients for {} [module: {}] in {}s",
				clients.size(), repository, moduleId, sw.elapsed().toSeconds());

		try {
			Files.createDirectories(cacheFile.getParent());
			objectMapper.writeValue(cacheFile.toFile(), clients);
			logger.info("Serialized clients for {} [module: {}] in {}", repository, moduleId, cacheFile);
		} catch (IOException e) {
			logger.error(e);
		}

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

	private boolean clientsCacheIsValid(Path cacheFile) {
		if (Files.exists(cacheFile)) {
			Date modified = new Date(cacheFile.toFile().lastModified());
			Date now = Date.from(Instant.now());

			long daysDiff = TimeUnit.DAYS.convert(Math.abs(now.getTime() - modified.getTime()), TimeUnit.MILLISECONDS);
			return daysDiff <= clientsCacheExpirationDays;
		}

		return false;
	}

	private boolean isValidClient(GitHubClient client) {
		try {
			GHRepository candidate = gh.getRepository(String.format("%s/%s", client.owner(), client.name()));
			return !candidate.isFork() && !candidate.isArchived() && !candidate.isDisabled();
		} catch (IOException e) {
			return false;
		}
	}

	private Path clientsCacheFile(Repository repository, String moduleId) {
		return clientsCacheDirectory
			.resolve(repository.owner())
			.resolve(repository.name())
			.resolve(moduleId + "-clients.json")
			.toAbsolutePath();
	}
}
