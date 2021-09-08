package org.swat.maracas.rest;

import java.nio.file.Path;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

@Service
public class GithubService {
	private static final Logger logger = LogManager.getLogger(GithubService.class);

	public void cloneRemote(String url, String ref, Path dest) throws CloneException {
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
			// Let me please try-with-resource without a variable :(
		} catch (GitAPIException e) {
			// Rethrow unchecked
			throw new CloneException(e);
		}
	}
}
