package com.github.maracas.forges.build;

import java.nio.file.Path;

public interface Builder {
  Path build(Path buildFile) throws BuildException;
}
