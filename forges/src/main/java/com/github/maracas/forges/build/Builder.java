package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public interface Builder {
  void build() throws BuildException;
  Optional<Path> locateJar();

  static Builder of(BuildConfig config) throws BuildException {
    Objects.requireNonNull(config);

    if (config.getBasePath().resolve(MavenBuilder.BUILD_FILE).toFile().exists())
      return new MavenBuilder(config);
    if (config.getBasePath().resolve(GradleBuilder.BUILD_FILE).toFile().exists())
      return new GradleBuilder(config);

    throw new BuildException("Don't know how to build " + config.getBasePath());
  }
}
