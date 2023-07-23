package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradleBuilderTest {
	final Path validProject = Path.of("src/test/resources/gradle-project/");
	final Path errorProject = Path.of("src/test/resources/gradle-project-error/");
	final Path multiProject = Path.of("src/test/resources/gradle-multi-project/");
	final Path validTarget = validProject.resolve("build/");
	final Path errorTarget = errorProject.resolve("build/");

	@BeforeEach
	void setUp() {
		FileUtils.deleteQuietly(validTarget.toFile());
		FileUtils.deleteQuietly(errorTarget.toFile());
		FileUtils.deleteQuietly(multiProject.resolve("core/build").toFile());
		FileUtils.deleteQuietly(multiProject.resolve("extra/build").toFile());
	}

	@Test
	void build_validGradle_default() {
		Builder builder = new GradleBuilder(validProject);
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validGradle_withGoal() {
		BuildConfig configWithGoal = BuildConfig.newDefault();
		configWithGoal.addGoal("clean");
		Builder builder = new GradleBuilder(validProject, configWithGoal);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_validGradle_with_invalidProperty() {
		BuildConfig configWithProperty = BuildConfig.newDefault();
		configWithProperty.setProperty("--unknown-property", "");
		Builder builder = new GradleBuilder(validProject, configWithProperty);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_validGradle_with_validProperty() {
		BuildConfig configWithProperty = BuildConfig.newDefault();
		configWithProperty.setProperty("--dry-run", "");
		Builder builder = new GradleBuilder(validProject, configWithProperty);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_compileError() {
		Builder builder = new GradleBuilder(errorProject);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_validGradle_invalidGoal() {
		BuildConfig configWithInvalidGoal = BuildConfig.newDefault();
		configWithInvalidGoal.addGoal("nope");
		Builder builder = new GradleBuilder(validProject, configWithInvalidGoal);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_multi_core_default_with_version() {
		Builder builder = new GradleBuilder(multiProject, new BuildConfig(Path.of("core")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("core-0.1.0.jar"));
	}

	@Test
	void build_multi_extra_default() {
		Builder builder = new GradleBuilder(multiProject, new BuildConfig(Path.of("extra")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("extra.jar"));
	}

	@Test
	void build_multi_invalid() {
		Builder builder = new GradleBuilder(multiProject, new BuildConfig(Path.of("nope")));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}
}