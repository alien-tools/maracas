package com.github.maracas.forges.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractBuilder implements Builder {
	protected final BuildConfig config;

	protected AbstractBuilder(BuildConfig config) {
		Objects.requireNonNull(config);
		this.config = config;
	}

	@Override
	public Optional<Path> locateJar() {
		Path jar = config.basePath().resolve(config.jar());

		if (Files.exists(jar))
			return Optional.of(jar);
		else
			return Optional.empty();
	}

	protected void executeCommand(String... command) {
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(config.basePath().toFile());
			int exitCode = pb.start().waitFor();
			if (exitCode != 0)
				throw new BuildException("%s failed: %d".formatted(Arrays.asList(command), exitCode));
		} catch (IOException e) {
			throw new BuildException("Build failed, command was: " + Arrays.toString(command), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
