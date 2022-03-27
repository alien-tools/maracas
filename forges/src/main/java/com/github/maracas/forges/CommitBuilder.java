package com.github.maracas.forges;

import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
  private final Commit commit;
  private final Path clonePath;
  private Path buildFile;
  private Path sources;

  public CommitBuilder(Commit commit, Path clonePath) {
    Objects.requireNonNull(commit);
    Objects.requireNonNull(clonePath);

    this.commit = commit;
    this.clonePath = clonePath;
    this.sources = clonePath;
  }

  public Path clone() throws CloneException {
    Cloner cloner = Cloner.of(commit.repository());
    return cloner.clone(commit, clonePath);
  }

  public Optional<Path> build() throws BuildException {
    Builder builder = Builder.of(clonePath);
    builder.build();

    return builder.locateJar();
  }

  public Optional<Path> cloneAndBuild() throws CloneException, BuildException {
    clone();
    return build();
  }

  public void setSources(Path sources) {
    this.sources = sources;
  }

  public Path getSources() {
    return this.sources;
  }

  public Commit getCommit() {
    return this.commit;
  }

  public Path getClonePath() {
    return this.clonePath;
  }
}
