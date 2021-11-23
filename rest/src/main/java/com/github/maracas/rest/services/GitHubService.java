package com.github.maracas.rest.services;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.PullRequest;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Service
public class GitHubService {
	@Autowired
	private GitHub github;
	@Value("${maracas.breakbot-file:.breakbot.yml}")
	private String breakbotFile;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;

	private static final Logger logger = LogManager.getLogger(GitHubService.class);

	public Path cloneRepository(BreakbotConfig.GitHubRepository config) {
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

	public BreakbotConfig readBreakbotConfig(PullRequest pr) {
		try (InputStream configIn = getRepository(pr).getFileContent(breakbotFile).read()) {
			BreakbotConfig res = BreakbotConfig.fromYaml(configIn);
			if (res != null)
				return res;
		} catch (@SuppressWarnings("unused") IOException e) {
			logger.error(e);
		}

		return BreakbotConfig.defaultConfig();
	}

	public GHRepository getRepository(PullRequest pr) {
		return getRepository(pr.owner() + "/" + pr.repository());
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

	public GHPullRequest getPullRequest(PullRequest pr) {
		try {
			GHRepository repo = getRepository(pr);
			return repo.getPullRequest(pr.id());
		} catch (IOException e) {
			throw new GitHubException(
					"Couldn't fetch PR %d from repository %s/%s".formatted(pr.id(), pr.owner(), pr.repository()),
					e);
		}
	}

	public String getHead(PullRequest pr) {
		return getPullRequest(pr).getHead().getSha();
	}

	public String getBranchName(BreakbotConfig.GitHubRepository config) {
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
