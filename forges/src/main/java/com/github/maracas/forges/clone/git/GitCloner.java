package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import com.github.maracas.forges.Commit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * We would like to use JGit, but the lack of support for shallow clones hurts.
 * So we're using dirty Processes, trying to optimize network/cpu usage.
 */
public class GitCloner implements Cloner {
  @Override
  public Path clone(Commit commit, Path dest) {
    Objects.requireNonNull(commit);
    Objects.requireNonNull(dest);

    if (!dest.toFile().exists())
      dest.toFile().mkdirs();
    else
      return dest;

    String workingDirectory = dest.toAbsolutePath().toString();
    executeCommand("git", "-C", workingDirectory, "init");
    executeCommand("git", "-C", workingDirectory, "remote", "add", "origin", commit.repository().remoteUrl());
    executeCommand("git", "-C", workingDirectory, "fetch", "--depth", "1", "origin", commit.sha());
    executeCommand("git", "-C", workingDirectory, "checkout", "FETCH_HEAD");

    return dest;
  }

  @Override
  public Path clone(Repository repository, Path dest) {
    Objects.requireNonNull(repository);
    Objects.requireNonNull(dest);

    executeCommand(
      "git", "clone",
      "--depth", "1",
      "--single-branch",
      repository.remoteUrl(),
      dest.toAbsolutePath().toString()
    );

    return dest;
  }

  private void executeCommand(String... command) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      pb.environment().put("GIT_TERMINAL_PROMPT", "0");

      int exitCode = pb.start().waitFor();
      if (exitCode != 0)
        throw new CloneException("%s failed: %d".formatted(command, exitCode));
    } catch (IOException e) {
      throw new CloneException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
