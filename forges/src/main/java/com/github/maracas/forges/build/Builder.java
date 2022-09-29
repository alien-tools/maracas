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
  Map<String, Path> locateModules();

  static Builder of(BuildConfig config) throws BuildException {
    Objects.requireNonNull(config);

    if (MavenBuilder.isMavenProject(config.getBasePath()))
      return new MavenBuilder(config);
    if (GradleBuilder.isGradleProject(config.getBasePath()))
      return new GradleBuilder(config);

    throw new BuildException("Don't know how to build " + config.getBasePath());
  }
}
