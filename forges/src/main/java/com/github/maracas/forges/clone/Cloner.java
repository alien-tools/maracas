package com.github.maracas.forges.clone;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.git.GitCloner;

import java.nio.file.Path;
import java.util.Objects;

public interface Cloner {
  void clone(Commit commit, Path dest, int timeoutSeconds) throws CloneException;
  void clone(Repository repository, Path dest, int timeoutSeconds) throws CloneException;

  default void clone(Commit commit, Path dest) throws CloneException {
    clone(commit, dest, Integer.MAX_VALUE);
  }

  default void clone(Repository repository, Path dest) throws CloneException {
    clone(repository, dest, Integer.MAX_VALUE);
  }

  static Cloner of(Repository repository) {
    Objects.requireNonNull(repository);

    if (repository.remoteUrl().startsWith("https://github.com/"))
      return new GitCloner();

    throw new CloneException("Repository scheme " + repository.remoteUrl() + " not supported");
  }
}
