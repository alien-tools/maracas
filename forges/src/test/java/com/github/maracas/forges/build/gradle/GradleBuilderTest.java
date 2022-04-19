package com.github.maracas.forges.build.gradle;

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

class GradleBuilderTest {
	final Path validProject = Paths.get("src/test/resources/gradle-project/");
	final Path errorProject = Paths.get("src/test/resources/gradle-project-error/");
	final Path multiProject = Paths.get("src/test/resources/gradle-multi-project/");
	final Path validTarget = validProject.resolve("build/");
	final Path errorTarget = errorProject.resolve("build/");

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(validTarget.toFile());
		FileUtils.deleteDirectory(errorTarget.toFile());
		FileUtils.deleteDirectory(multiProject.resolve("core/build").toFile());
		FileUtils.deleteDirectory(multiProject.resolve("extra/build").toFile());
	}

	@Test
	void build_validGradle_default() {
		Builder builder = new GradleBuilder(new BuildConfig(validProject));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validGradle_withGoal() {
		BuildConfig configWithGoal = new BuildConfig(validProject);
		configWithGoal.addGoal("clean");
		Builder builder = new GradleBuilder(configWithGoal);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_validGradle_with_invalidProperty() {
		BuildConfig configWithProperty = new BuildConfig(validProject);
		configWithProperty.setProperty("--unknown-property", "");
		Builder builder = new GradleBuilder(configWithProperty);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_validGradle_with_validProperty() {
		BuildConfig configWithProperty = new BuildConfig(validProject);
		configWithProperty.setProperty("--dry-run", "");
		Builder builder = new GradleBuilder(configWithProperty);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_compileError() {
		Builder builder = new GradleBuilder(new BuildConfig(errorProject));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_validGradle_invalidGoal() {
		BuildConfig configWithInvalidGoal = new BuildConfig(validProject);
		configWithInvalidGoal.addGoal("nope");
		Builder builder = new GradleBuilder(configWithInvalidGoal);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}

	@Test
	void build_multi_core_default() {
		Builder builder = new GradleBuilder(new BuildConfig(multiProject, Paths.get("core")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("core.jar"));
	}

	@Test
	void build_multi_extra_default() {
		Builder builder = new GradleBuilder(new BuildConfig(multiProject, Paths.get("extra")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("extra.jar"));
	}

	@Test
	void build_multi_invalid() {
		Builder builder = new GradleBuilder(new BuildConfig(multiProject, Paths.get("nope")));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Gradle build failed"));
	}
}