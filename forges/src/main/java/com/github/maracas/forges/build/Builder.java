package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public interface Builder {
  void build() throws BuildException;
  void build(List<String> goals, Properties properties) throws BuildException;
  Path getBasePath();
  Optional<Path> locateJar();
  default Optional<Path> getFile(Path file) {
    if (!getBasePath().resolve(file).toAbsolutePath().toFile().exists())
      return Optional.empty();
    else
      return Optional.of(getBasePath().resolve(file).toAbsolutePath());
  }

  static Builder of(Path sources) throws BuildException {
    Objects.requireNonNull(sources);

    Path pom = sources.resolve("pom.xml");
    Path gradle = sources.resolve("gradlew");

    if (pom.toFile().exists() && pom.toFile().isFile())
      return new MavenBuilder(pom);
    if (gradle.toFile().exists() && gradle.toFile().isFile())
      return new GradleBuilder(gradle);

    throw new BuildException("No build file found in " + sources);
  }

  static Builder of(Path sources, Path buildFile) throws BuildException {
    Objects.requireNonNull(sources);
    Objects.requireNonNull(buildFile);

    Path full = sources.resolve(buildFile);
    if (full.toFile().exists() && full.toFile().isFile() && full.getFileName().toString().equals("pom.xml"))
      return new MavenBuilder(full);
    if (full.toFile().exists() && full.toFile().isFile() && full.getFileName().toString().equals("build.gradle"))
      return new GradleBuilder(full);

    throw new BuildException("No valid build file " + buildFile + " in " + sources);
  }
}
