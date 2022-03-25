package com.github.maracas.forges.clone;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;

import java.nio.file.Path;

public interface Cloner {
  Path clone(Commit commit, Path dest) throws CloneException;
  Path clone(Repository repository, Path dest) throws CloneException;
}
