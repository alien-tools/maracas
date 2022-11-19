package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.Package;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * There doesn't seem to be a way to skip tasks (e.g., "-x test"),
 * so we assume Gradle users will prepare an optimized task to be
 * invoked in their build.gradle
 */
public class GradleBuilder implements Builder {
	private final Path basePath;
	private final BuildConfig config;
	public static final String BUILD_FILE = "build.gradle";
	public static final List<String> DEFAULT_GOALS = List.of("build");
	public static final Properties DEFAULT_PROPERTIES = new Properties();
	private static final Logger logger = LogManager.getLogger(GradleBuilder.class);

	public static boolean isGradleProject(Path basePath) {
		return Files.exists(basePath.resolve(BUILD_FILE));
	}

	public GradleBuilder(Path basePath, BuildConfig config) {
		Objects.requireNonNull(basePath);
		Objects.requireNonNull(config);
		this.basePath = basePath;
		this.config = config;
	}

	public GradleBuilder(Path basePath) {
		Objects.requireNonNull(basePath);
		this.basePath = basePath;
		this.config = BuildConfig.newDefault();
	}

	@Override
	public void build(int timeoutSeconds) {
		logger.warn("GradleBuilder doesn't honor timeouts yet");

		Optional<Path> jar = locateJar();

		if (jar.isEmpty()) {
			List<String> goals = config.getGoals().isEmpty()
				? DEFAULT_GOALS
				: config.getGoals();
			Properties properties = config.getProperties().isEmpty()
				? DEFAULT_PROPERTIES
				: config.getProperties();

			try (ProjectConnection project = getProjectConnection(config)) {
				project
					.newBuild()
					.setStandardOutput(null)
					.setStandardError(IoBuilder.forLogger(logger).setLevel(Level.ERROR).buildOutputStream())
					.withArguments(properties.keySet().toArray(new String[0]))
					.forTasks(goals.toArray(new String[0]))
					.run();
			} catch (org.gradle.tooling.BuildException | org.gradle.tooling.exceptions.UnsupportedBuildArgumentException e) {
				throw new BuildException("Gradle build failed: %s".formatted(e.getMessage()), e);
			}
		} else logger.info("{} has already been built. Skipping.", basePath);
	}

	@Override
	public Optional<Path> locateJar() {
		try (ProjectConnection project = getProjectConnection(config)) {
			Map<String, String> props = readGradleProperties(project);
			Path buildPath = Path.of(props.getOrDefault("buildDir",
				basePath.resolve(config.getModule()).toAbsolutePath().toString()));
			Path libsPath = buildPath.resolve(props.getOrDefault("libsDirName", "libs"));
			String version = props.get("version");
			String versionQualifier = (version == null || "unspecified".equals(version))
				? ""
				: "-" + props.get("version");
			String jarName = props.getOrDefault("name", config.getModule().toString()) + versionQualifier + ".jar";
			Path jar = libsPath.resolve(jarName);

			if (Files.exists(jar))
				return Optional.of(jar);
			else
				return Optional.empty();
		} catch (org.gradle.tooling.BuildException | org.gradle.tooling.exceptions.UnsupportedBuildArgumentException e) {
			throw new BuildException("Gradle build failed: %s".formatted(e.getMessage()), e);
		}
	}

	@Override
	public List<Package> locatePackages() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private ProjectConnection getProjectConnection(BuildConfig config) {
		return GradleConnector.newConnector()
			.forProjectDirectory(basePath.resolve(config.getModule()).toFile())
			.connect();
	}

	private Map<String, String> readGradleProperties(ProjectConnection project) {
		Map<String, String> props = new HashMap<>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		project.newBuild()
			.setStandardOutput(baos)
			.forTasks("properties")
			.run();

		baos.toString().lines().forEach(l -> {
			String[] fields = l.split(": ");

			if (fields.length == 2)
				props.put(fields[0], fields[1]);
		});

		return props;
	}
}
