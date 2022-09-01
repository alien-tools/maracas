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
import java.util.Objects;

public class Client {
	private final Path sources;
	private final String label;
	@JsonIgnore
	private final Library used;
	@JsonIgnore
	private CtModel sourceModel = null;

	private static final Logger logger = LogManager.getLogger(Client.class);

	public Client(Path sources, Library used) {
		if (!PathHelpers.isValidDirectory(sources))
			throw new IllegalArgumentException("Not a valid source directory: " + sources);
		Objects.requireNonNull(used);

		this.sources = sources;
		this.used = used;
		this.label = sources.getFileName().toString();
	}

	public CtModel getSourceModel() {
		if (sourceModel == null)
			sourceModel = buildSpoonModel();
		return sourceModel;
	}

	private CtModel buildSpoonModel() {
		Stopwatch sw = Stopwatch.createStarted();
		Launcher launcher;

		// Attempting to get the proper source folders to analyze
		// We don't care about classpath beyond {@link #used} though, so just skip classpath creation
		if (Files.exists(sources.resolve("pom.xml")))
			launcher = new MavenLauncher(sources.toAbsolutePath().toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE, new String[0]);
		else if (Files.exists(sources.resolve("build.gradle")))
			launcher = new GradleLauncher(sources);
		else {
			launcher = new Launcher();
			launcher.getEnvironment().setComplianceLevel(11);
			launcher.addInputResource(sources.toAbsolutePath().toString());
		}

		// Ignore missing types/classpath related errors
		launcher.getEnvironment().setNoClasspath(true);
		// Proceed even if we find the same type twice; affects the precision of the result
		launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
		// Ignore files with syntax/JLS violations and proceed
		launcher.getEnvironment().setIgnoreSyntaxErrors(true);

		// Only classpath we care about is the library we're analyzing
		String[] cp = { used.getJar().toAbsolutePath().toString() };
		launcher.getEnvironment().setSourceClasspath(cp);

		CtModel model = launcher.buildModel();
		logger.info("Building Spoon model for {} took {}ms", this, sw.elapsed().toMillis());
		return model;
	}

	public Path getSources() {
		return sources;
	}

	public Library getUsed() {
		return used;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return String.format("Client %s [sources=%s used=%s]",
			label, sources, used.getLabel());
	}
}
