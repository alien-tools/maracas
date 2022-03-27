package com.github.maracas.forges.clone;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.git.GitCloner;

import java.nio.file.Path;
import java.util.Objects;

public interface Cloner {
  Path clone(Commit commit, Path dest) throws CloneException;
  Path clone(Repository repository, Path dest) throws CloneException;

  static Cloner of(Repository repository) {
    Objects.requireNonNull(repository);

    if (repository.remoteUrl().startsWith("https://github.com/"))
      return new GitCloner();

    throw new CloneException("Repository scheme " + repository.remoteUrl() + " not supported");
  }
}
