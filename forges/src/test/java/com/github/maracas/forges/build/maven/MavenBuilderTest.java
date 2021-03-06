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
	final Path multiProject = Paths.get("src/test/resources/maven-multi-project/");

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(validTarget.toFile());
		FileUtils.deleteDirectory(errorTarget.toFile());
		FileUtils.deleteDirectory(multiProject.resolve("core-module/target").toFile());
		FileUtils.deleteDirectory(multiProject.resolve("extra-module/target").toFile());
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

	@Test
	void build_multi_core_default() {
		Builder builder = new MavenBuilder(new BuildConfig(multiProject, Paths.get("core-module")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("core-module-0.0.2.jar"));
	}

	@Test
	void build_multi_extra_default() {
		Builder builder = new MavenBuilder(new BuildConfig(multiProject, Paths.get("extra-module")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("extra-module-0.0.3.jar"));
	}

	@Test
	void build_multi_invalid() {
		Builder builder = new MavenBuilder(new BuildConfig(multiProject, Paths.get("nope")));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Couldn't parse"));
	}
}
