package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * We would like to use JGit, but the lack of support for shallow clones hurts.
 * So we're using dirty Processes, trying to optimize network/cpu usage.
 */
public class GitCloner implements Cloner {
	private static final Logger logger = LogManager.getLogger(GitCloner.class);

	@Override
	public void clone(Commit commit, Path dest, int timeoutSeconds) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(dest);
		if (timeoutSeconds < 1)
			throw new IllegalArgumentException("timeoutSeconds < 1");

		if (dest.toFile().exists()) {
			logger.info("{} exists; skipping", dest);
		} else if (dest.toFile().mkdirs()) {
			try {
				Stopwatch sw = Stopwatch.createStarted();
				String workingDirectory = dest.toAbsolutePath().toString();
				logger.info("Cloning commit {} from {} into {}",
					commit::sha, () -> commit.repository().remoteUrl(), () -> dest);
				executeCommand(timeoutSeconds, "git", "-C", workingDirectory, "init");
				executeCommand(timeoutSeconds, "git", "-C", workingDirectory, "remote", "add", "origin", commit.repository().remoteUrl());
				executeCommand(timeoutSeconds, "git", "-C", workingDirectory, "fetch", "--depth", "1", "origin", commit.sha());
				executeCommand(timeoutSeconds, "git", "-C", workingDirectory, "checkout", "FETCH_HEAD");
				logger.info("Cloning commit {} from {} took {}ms",
					commit::sha, () -> commit.repository().remoteUrl(), () -> sw.elapsed().toMillis());
			} catch (CloneException e) {
				// If anything went wrong we need to clean up our dirty state and rethrow
				try {
					FileUtils.deleteDirectory(dest.toFile());
				} catch (IOException ee) {
					logger.error(ee);
				}

				throw e;
			}
		} else {
			throw new CloneException("Couldn't create clone directory %s".formatted(dest));
		}
	}

	@Override
	public void clone(Repository repository, Path dest, int timeoutSeconds) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(dest);
		if (timeoutSeconds < 1)
			throw new IllegalArgumentException("timeoutSeconds < 1");

		if (dest.toFile().exists()) {
			logger.info("{} exists; skipping", dest);
		} else if (dest.toFile().mkdirs()) {
			try {
				Stopwatch sw = Stopwatch.createStarted();
				logger.info("Cloning repository {} [{}] into {}",
					repository::remoteUrl, repository::branch, () -> dest);
				executeCommand(
					timeoutSeconds,
					"git", "clone",
					"--depth", "1",
					"--branch", repository.branch(),
					"--single-branch",
					repository.remoteUrl(),
					dest.toAbsolutePath().toString()
				);
				logger.info("Cloning repository {} [{}] took {}ms",
					repository::remoteUrl, repository::branch, () -> sw.elapsed().toMillis());
			} catch (Exception e) {
				// If anything went wrong we need to clean up our dirty state and rethrow
				try {
					FileUtils.deleteDirectory(dest.toFile());
				} catch (IOException ee) {
					logger.error(ee);
				}

				throw e;
			}
		} else {
			throw new CloneException("Couldn't create clone directory %s".formatted(dest));
		}
	}

	private void executeCommand(int timeout, String... command) throws CloneException {
		try {
			String readableCommand = String.join(" ", command);
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.environment().put("GIT_TERMINAL_PROMPT", "0"); // Don't ask me for my password

			Process proc = pb.start();
			boolean completed = proc.waitFor(timeout, TimeUnit.SECONDS);

			if (completed && proc.exitValue() != 0) {
				String errors = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
				logger.error("{} failed: {}", readableCommand, errors);
				throw new CloneException("%s failed (%d): %s".formatted(readableCommand, proc.exitValue(), errors));
			}

			if (!completed) {
				proc.destroy();
				// Waiting a bit for the process to actually terminate, if necessary
				proc.waitFor(5, TimeUnit.SECONDS);
				logger.error("{} timed out [> {}s]", readableCommand, timeout);
				throw new CloneException("%s timed out [> %ds]".formatted(readableCommand, timeout));
			}
		} catch (IOException e) {
			throw new CloneException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
