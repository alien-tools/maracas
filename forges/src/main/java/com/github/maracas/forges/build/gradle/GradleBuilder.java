package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class GradleBuilder implements Builder {
  private final Path gradle;
  private final Path base;

  public GradleBuilder(Path gradle) {
    Objects.requireNonNull(gradle);
    if (!gradle.toFile().exists())
      throw new BuildException("The gradle.build file doesn't exist: " + gradle);

    this.gradle = gradle.toAbsolutePath();
    this.base = gradle.getParent().toAbsolutePath();
  }

  @Override
  public void build() throws BuildException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build(List<String> goals, Properties properties) throws BuildException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Path> locateJar() {
    throw new UnsupportedOperationException();
  }
}
