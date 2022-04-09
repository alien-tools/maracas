package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.AbstractBuilder;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.gradle.tooling.GradleConnector;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * There doesn't seem to be a way to skip tasks (e.g., "-x test"),
 * so we assume Gradle users will prepare an optimized task to be
 * invoked in their build.gradle
 */
public class GradleBuilder extends AbstractBuilder {
	public static final String BUILD_FILE = "build.gradle";
	public static final List<String> DEFAULT_GOALS = List.of("build");
	public static final Properties DEFAULT_PROPERTIES = new Properties();
	private static final Logger logger = LogManager.getLogger(GradleBuilder.class);

	public static boolean isGradleProject(Path basePath) {
		return Files.exists(basePath.resolve(BUILD_FILE));
	}

	public GradleBuilder(BuildConfig config) {
		super(config);
	}

	@Override
	public void build() {
		Optional<Path> jar = locateJar();

		if (jar.isEmpty()) {
			List<String> goals = config.getGoals().isEmpty()
				? DEFAULT_GOALS
				: config.getGoals();
			Properties properties = config.getProperties().isEmpty()
				? DEFAULT_PROPERTIES
				: config.getProperties();

			try {
				Stopwatch sw = Stopwatch.createStarted();
				GradleConnector.newConnector()
					.forProjectDirectory(config.getBasePath().toFile())
					.connect()
					.newBuild()
					.setStandardOutput(null)
					.setStandardError(IoBuilder.forLogger(logger).setLevel(Level.ERROR).buildOutputStream())
					.withArguments(properties.keySet().toArray(new String[0]))
					.forTasks(goals.toArray(new String[0]))
					.run();
			} catch (org.gradle.tooling.BuildException | org.gradle.tooling.exceptions.UnsupportedBuildArgumentException e) {
				throw new BuildException("Gradle build failed: %s".formatted(e.getMessage()), e);
			}
		} else logger.info("{} has already been built. Skipping.", config.getBasePath());
	}

	@Override
	public Optional<Path> locateJar() {
		Path jar = config.getBasePath()
			.resolve("build")
			.resolve("libs")
			.resolve(config.getBasePath().getFileName().toString() + ".jar");

		if (Files.exists(jar))
			return Optional.of(jar);
		else
			return Optional.empty();
	}
}
