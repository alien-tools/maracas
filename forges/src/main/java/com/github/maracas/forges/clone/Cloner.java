package com.github.maracas.forges.clone;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;

import java.nio.file.Path;

public interface Cloner {
	void clone(Commit commit, Path dest, int timeoutSeconds) throws CloneException;

	void clone(Repository repository, Path dest, int timeoutSeconds) throws CloneException;

	default void clone(Commit commit, Path dest) throws CloneException {
		clone(commit, dest, Integer.MAX_VALUE);
	}

	default void clone(Repository repository, Path dest) throws CloneException {
		clone(repository, dest, Integer.MAX_VALUE);
	}
}
