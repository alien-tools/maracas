package com.github.maracas.rest.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.maracas.rest.breakbot.GithubRepositoryConfig;

@Service
public class GitHubService {
	@Autowired
	private GitHub github;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;

	private static final Logger logger = LogManager.getLogger(GitHubService.class);

	public Path cloneRepository(GithubRepositoryConfig config) {
		GHRepository repo = getRepository(config.repository());

		String branch = getBranchName(config);
		String sha =
			StringUtils.isEmpty(config.sha()) ? getBranch(repo, branch).getSHA1() : config.sha();

		Path dest = Paths.get(clonePath)
			.resolve(String.valueOf(repo.getId()))
			.resolve(sha);

		logger.info("Cloning repository {} [branch={}, sha={}] to {}",
			config.repository(), branch, sha, dest);
		cloneRemote(repo.getHttpTransportUrl(), branch, sha, dest);
		return dest;
	}

	public GHRepository getRepository(String owner, String repository) {
		return getRepository(owner + "/" + repository);
	}

	public GHRepository getRepository(String fullName) {
		try {
			return github.getRepository(fullName);
		} catch (IOException e) {
			throw new GitHubException(
					"Couldn't fetch repository %s".formatted(fullName),
					e);
		}
	}

	public GHPullRequest getPullRequest(String owner, String repository, int prId) {
		try {
			GHRepository repo = getRepository(owner, repository);
			return repo.getPullRequest(prId);
		} catch (IOException e) {
			throw new GitHubException(
					"Couldn't fetch PR %d from repository %s/%s".formatted(prId, owner, repository),
					e);
		}
	}

	public GHPullRequest getPullRequest(GHRepository repository, int prId) {
		try {
			return repository.getPullRequest(prId);
		} catch (IOException e) {
			throw new GitHubException(
					"Couldn't fetch PR %d from repository %s".formatted(prId, repository.getFullName()),
					e);
		}
	}

	public String getBranchName(GithubRepositoryConfig config) {
		GHRepository repo = getRepository(config.repository());
		String defaultBranch = repo.getDefaultBranch();

		return StringUtils.isEmpty(config.branch()) ? defaultBranch : config.branch();
	}

	public GHBranch getBranch(GHRepository repository, String name) {
		try {
			return repository.getBranch(name);
		} catch (IOException e) {
			throw new GitHubException(
					"Couldn't fetch branch %s from repository %s".formatted(name, repository.getFullName()),
					e);
		}
	}

	public void cloneRemote(String url, String ref, String sha, Path dest) {
		if (dest.toFile().exists()) {
			logger.info("{} exists. Skipping.", dest);
			return;
		}

		logger.info("Cloning {} [{}]", url, ref);
		String fullRef = "refs/heads/" + ref;
		CloneCommand clone =
			Git.cloneRepository()
				.setURI(url)
				.setBranchesToClone(Collections.singletonList(fullRef))
				.setBranch(fullRef)
				.setDirectory(dest.toFile());

		try (Git g = clone.call()) {
			if (!StringUtils.isEmpty(sha)) {
				g.checkout()
					.setName(sha)
					.call();
			}
		} catch (GitAPIException e) {
			// Rethrow unchecked
			throw new CloneException(e);
		}
	}
}
