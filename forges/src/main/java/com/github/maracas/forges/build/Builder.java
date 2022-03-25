package com.github.maracas.forges.build;

import java.nio.file.Path;

public interface Builder {
  Path build() throws BuildException;
  Path locateJar();
  Path locateSources();
}
