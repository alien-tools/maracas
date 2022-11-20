package com.github.maracas.forges.build;

import com.github.maracas.forges.Package;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface Builder {
	void build(int timeoutSeconds) throws BuildException;

	Optional<Path> locateJar();

	List<Package> locatePackages();

	default void build() throws BuildException {
		build(Integer.MAX_VALUE);
	}
}
