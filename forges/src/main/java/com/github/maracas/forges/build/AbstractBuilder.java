package com.github.maracas.forges.build;

import java.nio.file.Files;
import java.util.Objects;

public abstract class AbstractBuilder implements Builder {
	protected final BuildConfig config;

	protected AbstractBuilder(BuildConfig config) {
		Objects.requireNonNull(config);
		this.config = config;
	}
}
