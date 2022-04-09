package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
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
		Builder builder = new MavenBuilder(new BuildConfig(validProject));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withGoal() {
		BuildConfig configWithGoal = new BuildConfig(validProject);
		configWithGoal.addGoal("clean");
		Builder builder = new MavenBuilder(configWithGoal);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withProperty() {
		BuildConfig configWithProperty = new BuildConfig(validProject);
		configWithProperty.setProperty("maven.compiler.source", "42");
		Builder builder = new MavenBuilder(configWithProperty);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("invalid source release: 42"));
	}

	@Test
	void build_compileError() {
		Builder builder = new MavenBuilder(new BuildConfig(errorProject));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("COMPILATION ERROR"));
	}

	@Test
	void build_invalidGoal() {
		BuildConfig configWithInvalidGoal = new BuildConfig(validProject);
		configWithInvalidGoal.addGoal("nope");
		Builder builder = new MavenBuilder(configWithInvalidGoal);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Unknown lifecycle phase \"nope\""));
	}
}
