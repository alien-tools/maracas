package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.Builder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenBuilderTest {
	final Path validProject = Path.of("src/test/resources/maven-project/");
	final Path validTarget = validProject.resolve("target/");
	final Path errorProject = Path.of("src/test/resources/maven-project-error/");
	final Path errorTarget = errorProject.resolve("target/");
	final Path multiProject = Path.of("src/test/resources/maven-multi-project/");
	final Path invalidProject = Path.of("src/test/resources/gradle-project");

	@BeforeEach
	void setUp() {
		FileUtils.deleteQuietly(validTarget.toFile());
		FileUtils.deleteQuietly(errorTarget.toFile());
		FileUtils.deleteQuietly(multiProject.resolve("core-module/target").toFile());
		FileUtils.deleteQuietly(multiProject.resolve("extra-module/target").toFile());
	}

	@Test
	void build_validPom_default() {
		Builder builder = new MavenBuilder(validProject);
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withGoal() {
		BuildConfig configWithGoal = BuildConfig.newDefault();
		configWithGoal.addGoal("clean");
		Builder builder = new MavenBuilder(validProject, configWithGoal);
		builder.build();
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withProperty() {
		BuildConfig configWithProperty = BuildConfig.newDefault();
		configWithProperty.setProperty("maven.compiler.source", "42");
		Builder builder = new MavenBuilder(validProject, configWithProperty);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("invalid source release: 42"));
	}

	@Test
	void build_compileError() {
		Builder builder = new MavenBuilder(errorProject);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("COMPILATION ERROR"));
	}

	@Test
	void build_invalidGoal() {
		BuildConfig configWithInvalidGoal = BuildConfig.newDefault();
		configWithInvalidGoal.addGoal("nope");
		Builder builder = new MavenBuilder(validProject, configWithInvalidGoal);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Unknown lifecycle phase \"nope\""));
	}

	@Test
	void build_no_pom() {
		Builder builder = new MavenBuilder(invalidProject);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Couldn't find pom.xml"));
	}

	@Test
	void build_multi_core_default() {
		Builder builder = new MavenBuilder(multiProject, new BuildConfig(Path.of("core-module")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("core-module-0.0.2.jar"));
	}

	@Test
	void build_multi_extra_default() {
		Builder builder = new MavenBuilder(multiProject, new BuildConfig(Path.of("extra-module")));
		builder.build();
		assertTrue(builder.locateJar().isPresent());
		assertTrue(builder.locateJar().get().getFileName().endsWith("extra-module-0.0.3.jar"));
	}

	@Test
	void build_multi_invalid() {
		Builder builder = new MavenBuilder(multiProject, new BuildConfig(Path.of("nope")));
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("Couldn't find module nope"));
	}

	@Test
	void build_maracas_timeout() {
		Builder builder = new MavenBuilder(Path.of("../"));
		Exception thrown = assertThrows(BuildException.class, () -> builder.build(Duration.ofSeconds(1)));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void locate_modules_valid() {
		Builder builder = new MavenBuilder(validProject);
		List<BuildModule> modules = builder.locateModules();

		assertThat(modules, contains(new BuildModule("test:maven-project", Path.of(""))));
	}

	@Test
	void locate_modules_multi() {
		Builder builder = new MavenBuilder(multiProject);
		List<BuildModule> modules = builder.locateModules();

		assertThat(modules, containsInAnyOrder(
				new BuildModule("sample:parent-module", Path.of("")),
				new BuildModule("sample:core-module", Path.of("core-module")),
				new BuildModule("sample:extra-module", Path.of("extra-module"))
		));
	}
}
