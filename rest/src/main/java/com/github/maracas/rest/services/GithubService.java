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
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.maracas.rest.breakbot.GithubRepositoryConfig;

@Service
public class GithubService {
	@Autowired
	private GitHub github;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;

	private static final Logger logger = LogManager.getLogger(GithubService.class);

	public Path cloneRepository(GithubRepositoryConfig config) throws IOException {
		GHRepository repo = github.getRepository(config.repository());

		String branch = getBranch(config);
		String sha =
			StringUtils.isEmpty(config.sha()) ? repo.getBranch(branch).getSHA1() : config.sha();

		Path dest = Paths.get(clonePath)
			.resolve(String.valueOf(repo.getId()))
			.resolve(sha);

		logger.info("Cloning repository {} [branch={}, sha={}] to {}",
			config.repository(), branch, sha, dest);
		cloneRemote(repo.getHttpTransportUrl(), branch, sha, dest);
		return dest;
	}

	public String getBranch(GithubRepositoryConfig config) throws IOException {
		GHRepository repo = github.getRepository(config.repository());
		String defaultBranch = repo.getDefaultBranch();

		return StringUtils.isEmpty(config.branch()) ? defaultBranch : config.branch();
	}

	public void cloneRemote(String url, String ref, String sha, Path dest) throws CloneException {
		if (dest.toFile().exists()) {
			logger.info("{} exists. Skipping.", dest);
			return;
		}

		logger.info("Cloning {} [{}]", url, ref);
		String fullRef = "refs/heads/" + ref; // FIXME?
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
