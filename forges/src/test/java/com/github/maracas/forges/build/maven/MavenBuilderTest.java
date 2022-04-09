package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MavenBuilderTest {
	final Path validproject = Paths.get("src/test/resources/maven-project/");
	final Path validPom = validproject.resolve("pom.xml");
	final Path validTarget = validproject.resolve("target/");
	final Path errorProject = Paths.get("src/test/resources/maven-project-error/");
	final Path errorPom = errorProject.resolve("pom.xml");
	final Path errorTarget = errorProject.resolve("target/");

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(validTarget.toFile());
		FileUtils.deleteDirectory(errorTarget.toFile());
	}

	@Test
	void build_validPom_default() {
		MavenBuilder builder = new MavenBuilder(validPom);
		builder.build();
		assertTrue(builder.locateJar().isPresent());
	}

	@Test
	void build_validPom_withGoal() {
		MavenBuilder builder = new MavenBuilder(validPom);
		builder.build(List.of("clean"), MavenBuilder.DEFAULT_PROPERTIES);
		assertFalse(builder.locateJar().isPresent());
	}

	@Test
	void build_compileError() {
		MavenBuilder builder = new MavenBuilder(errorPom);
		Exception thrown = assertThrows(BuildException.class, builder::build);
		assertThat(thrown.getMessage(), containsString("COMPILATION ERROR"));
	}

	@Test
	void build_invalidPom() {
		Path pom = Paths.get("nope/pom.xml");
		Exception thrown = assertThrows(BuildException.class, () -> new MavenBuilder(pom));
		assertThat(thrown.getMessage(), is("The pom file doesn't exist: nope/pom.xml"));
	}

	@Test
	void build_invalidGoal() {
		MavenBuilder builder = new MavenBuilder(validPom);
		Exception thrown = assertThrows(BuildException.class, () ->
			builder.build(List.of("nope"), MavenBuilder.DEFAULT_PROPERTIES)
		);
		assertThat(thrown.getMessage(), containsString("Unknown lifecycle phase \"nope\""));
	}

	@Test
	void build_noGoal() {
		MavenBuilder builder = new MavenBuilder(validPom);
		Exception thrown = assertThrows(BuildException.class, () ->
			builder.build(Collections.emptyList(), MavenBuilder.DEFAULT_PROPERTIES)
		);
		assertThat(thrown.getMessage(), containsString("No goals have been specified for this build"));
	}
}