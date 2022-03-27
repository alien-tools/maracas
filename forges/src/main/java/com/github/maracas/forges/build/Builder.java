package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public interface Builder {
  void build() throws BuildException;
  Optional<Path> locateJar();
  Path locateSources();

  static Builder of(Path sources) throws BuildException {
    Objects.requireNonNull(sources);

    Path pom = sources.resolve("pom.xml");
    Path gradle = sources.resolve("build.gradle");

    if (pom.toFile().exists() && pom.toFile().isFile())
      return new MavenBuilder(pom);
    if (gradle.toFile().exists() && gradle.toFile().isFile())
      return new GradleBuilder(gradle);

    throw new BuildException("No build file found in " + sources);
  }
}
