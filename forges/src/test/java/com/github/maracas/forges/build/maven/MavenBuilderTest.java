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

import static org.junit.jupiter.api.Assertions.*;

class MavenBuilderTest {
  final Path testProject = Paths.get("src/test/resources/maven-project/");
  final Path validPom = testProject.resolve("pom.xml");
  final Path validTarget = testProject.resolve("target/");
  final Path testProjectError = Paths.get("src/test/resources/maven-project-error/");

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(validTarget.toFile());
  }

  @Test
  void build_validPom_default() {
    MavenBuilder builder = new MavenBuilder(validPom);
    builder.build();
    System.out.println(builder.locateJar());
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
    MavenBuilder builder = new MavenBuilder(testProjectError);
    assertThrows(BuildException.class, () ->
      builder.build()
    );
  }

  @Test
  void build_invalidPom() {
    Path pom = Paths.get("nope/pom.xml");
    assertThrows(BuildException.class, () -> new MavenBuilder(pom));
  }

  @Test
  void build_invalidGoal() {
    MavenBuilder builder = new MavenBuilder(validPom);
    assertThrows(BuildException.class, () ->
      builder.build(List.of("nope"), MavenBuilder.DEFAULT_PROPERTIES)
    );
  }

  @Test
  void build_noGoal() {
    MavenBuilder builder = new MavenBuilder(validPom);
    assertThrows(BuildException.class, () ->
      builder.build(Collections.emptyList(), MavenBuilder.DEFAULT_PROPERTIES)
    );
  }

  @Test
  void locateSources() {
    MavenBuilder builder = new MavenBuilder(validPom);
    assertEquals(validPom.getParent().resolve("src/main/java").toAbsolutePath(), builder.locateSources());
  }
}