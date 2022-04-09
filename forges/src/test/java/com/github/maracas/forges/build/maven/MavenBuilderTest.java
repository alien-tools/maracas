package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MavenBuilderTest {
	final Path validProject = Paths.get("src/test/resources/maven-project/");
	final Path validTarget = validProject.resolve("target/");
	final Path errorProject = Paths.get("src/test/resources/maven-project-error/");
	final Path errorTarget = errorProject.resolve("target/");

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(validTarget.toFile());
		FileUtils.deleteDirectory(errorTarget.toFile());
	}

	@Test
	void build_validPom_default() {
		MavenBuilder builder = new MavenBuilder(new BuildConfig(validProject));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withGoal() {
		BuildConfig config = new BuildConfig(validProject);
		config.addGoal("clean");
		MavenBuilder builder = new MavenBuilder(config);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withProperty() {
		BuildConfig config = new BuildConfig(validProject);
		config.setProperty("maven.compiler.source", "42");
		MavenBuilder builder = new MavenBuilder(config);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("invalid source release: 42"));
	}

	@Test
	void build_compileError() {
		MavenBuilder builder = new MavenBuilder(new BuildConfig(errorProject));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("COMPILATION ERROR"));
	}

	@Test
	void build_invalidGoal() {
		BuildConfig config = new BuildConfig(validProject);
		config.addGoal("nope");
		MavenBuilder builder = new MavenBuilder(config);
		Exception thrown = assertThrows(BuildException.class, () -> builder.build());
		assertThat(thrown.getMessage(), containsString("Unknown lifecycle phase \"nope\""));
	}
}
