package com.github.maracas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.maracas.util.GradleLauncher;
import com.github.maracas.util.PathHelpers;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SourcesDirectory {
	private final Path location;
	@JsonIgnore
	private List<Path> classpath = Collections.emptyList();

	private static final Logger logger = LogManager.getLogger(SourcesDirectory.class);

	public SourcesDirectory(Path location) {
		if (!PathHelpers.isValidDirectory(location))
			throw new IllegalArgumentException("Not a valid source directory: " + location);

		this.location = location.toAbsolutePath();
	}

	public void setClasspath(List<Path> classpath) {
		this.classpath = Objects.requireNonNull(classpath);
	}

	public CtModel buildModel() {
		Stopwatch sw = Stopwatch.createStarted();
		Launcher launcher;

		// Attempting to get the proper source folders to analyze
		// We don't care about classpath beyond what's passed though, so just skip classpath creation
		if (Files.exists(location.resolve("pom.xml")))
			launcher = new MavenLauncher(location.toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE, new String[0]);
		else if (Files.exists(location.resolve("build.gradle")))
			launcher = new GradleLauncher(location);
		else {
			launcher = new Launcher();
			launcher.getEnvironment().setComplianceLevel(11);
			launcher.addInputResource(location.toString());
		}

		// Ignore missing types/classpath related errors
		launcher.getEnvironment().setNoClasspath(true);
		// Proceed even if we find the same type twice; affects the precision of the result
		launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
		// Ignore files with syntax/JLS violations and proceed
		launcher.getEnvironment().setIgnoreSyntaxErrors(true);

		// Only classpath we care about is what's given to us
		String[] cp = classpath.stream().map(p -> p.toAbsolutePath().toString()).toList().toArray(new String[0]);
		launcher.getEnvironment().setSourceClasspath(cp);

		CtModel spoonModel = launcher.buildModel();
		logger.info("Building Spoon model for {} [classpath={}] took {}ms", this, classpath, sw.elapsed().toMillis());
		return spoonModel;
	}

	public Path getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return String.format("SourceDirectory %s", location);
	}
}
