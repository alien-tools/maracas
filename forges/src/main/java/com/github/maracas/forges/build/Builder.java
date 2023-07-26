package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface Builder {
  void build(Duration timeout) throws BuildException;
  Optional<Path> locateJar();
  List<BuildModule> locateModules();

  default void build() throws BuildException {
    build(Duration.ofSeconds(Integer.MAX_VALUE));
  }

  static Builder of(CommitBuilder builder) throws BuildException {
    Objects.requireNonNull(builder);

    return Builder.of(builder.getClonePath(), builder.getBuildConfig());
  }

  static Builder of(Path basePath, BuildConfig buildConfig) throws BuildException {
    Objects.requireNonNull(basePath);
    Objects.requireNonNull(buildConfig);

    if (MavenBuilder.isMavenProject(basePath))
      return new MavenBuilder(basePath, buildConfig);
    if (GradleBuilder.isGradleProject(basePath))
      return new GradleBuilder(basePath, buildConfig);

    throw new BuildException("Don't know how to build " + basePath);
  }
}
