package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface Builder {
  void build() throws BuildException;
  Optional<Path> locateJar();
  Map<Path, String> locateModules();

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
