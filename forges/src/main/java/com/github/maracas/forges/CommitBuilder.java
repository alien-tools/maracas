package com.github.maracas.forges;

import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class CommitBuilder {
  private final Commit commit;
  private final Path clonePath;
  private Path sources;
  private Path buildFile;
  private Properties buildProperties = new Properties();
  private List<String> buildGoals = Collections.emptyList();

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

    if (!buildGoals.isEmpty() || !buildProperties.isEmpty())
      builder.build(buildGoals, buildProperties);
    else
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

  public void setBuildProperties(Properties properties) {
    this.buildProperties = properties;
  }

  public void setBuildGoals(List<String> goals) {
    this.buildGoals = goals;
  }

  public Commit getCommit() {
    return this.commit;
  }
}
