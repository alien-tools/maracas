package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;

import java.nio.file.Path;
import java.util.Objects;

public class BuilderFactory {
	public Builder create(Path basePath, BuildConfig buildConfig) throws BuildException {
		Objects.requireNonNull(basePath);
		Objects.requireNonNull(buildConfig);

		if (MavenBuilder.isMavenProject(basePath))
			return new MavenBuilder(basePath, buildConfig);
		if (GradleBuilder.isGradleProject(basePath))
			return new GradleBuilder(basePath, buildConfig);

		throw new BuildException("Don't know how to build " + basePath);
	}
}
