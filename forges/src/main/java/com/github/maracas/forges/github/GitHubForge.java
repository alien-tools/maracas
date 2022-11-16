package com.github.maracas.forges.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GitHubForge implements Forge {
	private final GitHub gh;
	private Path clientsCacheDirectory;
	private int clientsCacheExpirationDays = 1;

	private static final Logger logger = LogManager.getLogger(GitHubForge.class);

	public GitHubForge(GitHub gh) {
		this.gh = gh;

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
	public List<Repository> fetchTopStarredClients(Repository repository, String packageId, int limit, int minStars) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(packageId);

		List<GitHubClientsFetcher.Client> clients = fetchClients(repository, packageId);

		return clients
			.stream()
			.sorted(Comparator.comparingInt(GitHubClientsFetcher.Client::stars).reversed())
			.filter(client -> {
				// FIXME: Kinda harsh, but there are too many "unofficial" forks
				if (client.name().equals(repository.name()))
					return false;

				if (client.stars() < minStars)
					return false;

				try {
					GHRepository candidate = gh.getRepository(String.format("%s/%s", client.owner(), client.name()));
					return !candidate.isFork() && !candidate.isArchived() && !candidate.isDisabled();
				} catch (IOException e) {
					return false;
				}
			})
			.limit(limit > 0 ? limit : clients.size())
			.map(client -> fetchRepository(client.owner(), client.name()))
			.toList();
	}

	public List<GitHubClientsFetcher.Client> fetchClients(Repository repository, String packageId) {
		File cacheFile = clientsCacheFile(repository, packageId);
		ObjectMapper objectMapper = new ObjectMapper();

		if (clientsCacheIsValid(cacheFile)) {
			try {
				List<GitHubClientsFetcher.Client> clients = objectMapper.readValue(cacheFile, new TypeReference<>(){});
				logger.info("Fetched {} total clients for {} [package: {}] from {}",
					clients.size(), repository, packageId, cacheFile);
				return clients;
			} catch (IOException e) {
				logger.error(e);
			}
		}

		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Client> clients = fetcher.fetchClients(packageId);
		logger.info("Fetched {} total clients for {} [package: {}] in {}s",
			clients.size(), repository, packageId, sw.elapsed().toSeconds());

		try {
			cacheFile.getParentFile().mkdirs();
			objectMapper.writeValue(cacheFile, clients);
			logger.info("Serialized clients for {} [package: {}] in {}", repository, packageId, cacheFile);
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
		if (dir == null || !dir.toFile().exists())
			throw new IllegalArgumentException("dir does not exist");

		this.clientsCacheDirectory = dir;
	}

	private boolean clientsCacheIsValid(File cacheFile) {
		if (cacheFile.exists()) {
			Date modified = new Date(cacheFile.lastModified());
			Date now = Date.from(Instant.now());

			long daysDiff = TimeUnit.DAYS.convert(Math.abs(now.getTime() - modified.getTime()), TimeUnit.MILLISECONDS);
			return daysDiff <= clientsCacheExpirationDays;
		}

		return false;
	}

	private File clientsCacheFile(Repository repository, String packageId) {
		return clientsCacheDirectory
			.resolve(repository.owner())
			.resolve(repository.name())
			.resolve(packageId + "-clients.json")
			.toAbsolutePath()
			.toFile();
	}
}
