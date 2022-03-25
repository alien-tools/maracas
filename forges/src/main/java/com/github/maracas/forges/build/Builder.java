package com.github.maracas.forges.build;

import java.nio.file.Path;
import java.util.Optional;

public interface Builder {
  void build() throws BuildException;
  Optional<Path> locateJar();
  Path locateSources();
}
