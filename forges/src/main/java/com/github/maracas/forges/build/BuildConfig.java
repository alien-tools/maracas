package com.github.maracas.forges.build;

import java.nio.file.Path;
import java.util.Objects;

public record BuildConfig(
	Path basePath,
	String args,
	Path jar
) {
	public BuildConfig {
		Objects.requireNonNull(basePath);
		Objects.requireNonNull(args);
		Objects.requireNonNull(jar);
	}
}
