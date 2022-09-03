package com.github.maracas.util;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.SourceDirectory;
import org.gradle.tooling.model.eclipse.EclipseProject;
import spoon.Launcher;
import spoon.SpoonException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GradleLauncher extends Launcher {
	public GradleLauncher(Path gradleProject) {
		init(gradleProject);
	}

	public void init(Path gradleProject) {
		if (!Files.exists(gradleProject))
			throw new SpoonException(gradleProject + " does not exist");

		try (
			ProjectConnection project = GradleConnector.newConnector()
				.forProjectDirectory(gradleProject.toFile())
				.connect()
		) {
			EclipseProject model = project.getModel(EclipseProject.class);

			int javaVersion = model.getJavaSourceSettings() != null
				? Integer.parseInt(model.getJavaSourceSettings().getSourceLanguageLevel().getMajorVersion())
				: 11;

			List<File> sourceDirectories = model.getSourceDirectories()
				.stream()
				.filter(d -> !d.getPath().contains("test")) // FIXME: better way to distinguish?
				.map(SourceDirectory::getDirectory)
				.toList();

			String[] classpath = model.getClasspath()
				.stream()
				.map(entry -> entry.getFile().getAbsolutePath())
				.toArray(String[]::new);

			factory.getEnvironment().setNoClasspath(false);
			getModelBuilder().setSourceClasspath(classpath);
			getEnvironment().setComplianceLevel(javaVersion);
			sourceDirectories.forEach(dir -> addInputResource(dir.getAbsolutePath()));
		} catch (Exception e) {
			throw new SpoonException("Unable to parse build.gradle", e);
		}
	}
}

