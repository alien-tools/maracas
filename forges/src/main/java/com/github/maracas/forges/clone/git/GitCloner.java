package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * We would like to use JGit, but the lack of support for shallow clones hurts.
 * So we're using dirty Processes, trying to optimize network/cpu usage.
 */
public class GitCloner implements Cloner {
	private static final Logger logger = LogManager.getLogger(GitCloner.class);

	@Override
	public Path clone(Commit commit, Path dest) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(dest);

		if (!dest.toFile().exists())
			dest.toFile().mkdirs();
		else
			return dest;

		Stopwatch sw = Stopwatch.createStarted();
		String workingDirectory = dest.toAbsolutePath().toString();
		executeCommand("git", "-C", workingDirectory, "init");
		executeCommand("git", "-C", workingDirectory, "remote", "add", "origin", commit.repository().remoteUrl());
		executeCommand("git", "-C", workingDirectory, "fetch", "--depth", "1", "origin", commit.sha());
		executeCommand("git", "-C", workingDirectory, "checkout", "FETCH_HEAD");
		logger.info("Cloning commit {} from {} took {}ms",
			commit.sha(), commit.repository().remoteUrl(), sw.elapsed().toMillis());

		return dest;
	}

	@Override
	public Path clone(Repository repository, Path dest) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(dest);

		Stopwatch sw = Stopwatch.createStarted();
		executeCommand(
			"git", "clone",
			"--depth", "1",
			"--branch", repository.branch(),
			"--single-branch",
			repository.remoteUrl(),
			dest.toAbsolutePath().toString()
		);
		logger.info("Cloning repository {} [{}] took {}ms",
			repository.remoteUrl(), repository.branch(), sw.elapsed().toMillis());

		return dest;
	}

	private void executeCommand(String... command) {
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.environment().put("GIT_TERMINAL_PROMPT", "0");

			Process proc = pb.start();
			String errors = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
			int exitCode = proc.waitFor();

			if (exitCode != 0) {
				String readableCommand = Arrays.asList(command).stream().collect(joining(" "));
				logger.error("{} failed: {}", readableCommand, errors);
				throw new CloneException("%s failed (%d): %s".formatted(readableCommand, exitCode, errors));
			}
		} catch (IOException e) {
			throw new CloneException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
