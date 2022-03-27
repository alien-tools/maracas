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
  }

  public Path cloneCommit() throws CloneException {
    Cloner cloner = Cloner.of(commit.repository());
    return cloner.clone(commit, clonePath);
  }

  public Optional<Path> buildCommit() throws BuildException {
    Builder builder =
      buildFile != null ?
        Builder.of(clonePath, buildFile) :
        Builder.of(clonePath);

    if (!buildGoals.isEmpty() || !buildProperties.isEmpty())
      builder.build(buildGoals, buildProperties);
    else
      builder.build();

    return builder.locateJar();
  }

  public Optional<Path> cloneAndBuildCommit() throws CloneException, BuildException {
    cloneCommit();
    return buildCommit();
  }

  public Path getSources() {
    if (sources != null && clonePath.resolve(sources).toFile().exists())
      return clonePath.resolve(sources);
    if (clonePath.resolve("src/main/java").toFile().exists())
      return clonePath.resolve("src/main/java");
    else if (clonePath.resolve("src/").toFile().exists())
      return clonePath.resolve("src");
    else
      return clonePath;
  }

  public void setSources(Path sources) {
    this.sources = sources;
  }

  public void setBuildFile(Path buildFile) {
    this.buildFile = buildFile;
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

  public Path getClonePath() {
    return this.clonePath;
  }
}
