package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenBuilderTest {
  MavenBuilder maven;
  final Path testProject = Paths.get("src/test/resources/maven-project/");
  final Path validPom = testProject.resolve("pom.xml");
  final Path validTarget = testProject.resolve("target/");
  final Path validJar = validTarget.resolve("maven-project-1.0.0.jar");
  final Path testProjectError = Paths.get("src/test/resources/maven-project-error/");

  @BeforeEach
  void setUp() throws IOException {
    maven = new MavenBuilder();
    FileUtils.deleteDirectory(validTarget.toFile());
  }

  @Test
  void build_validPom_default() {
    maven.build(validPom);
    assertTrue(validJar.toFile().exists());
  }

  @Test
  void build_validPom_withGoal() {
    assertThrows(BuildException.class, () ->
      maven.build(validPom, List.of("clean"), MavenBuilder.DEFAULT_PROPERTIES)
    );
  }

  @Test
  void build_compileError() {
    assertThrows(BuildException.class, () ->
      maven.build(testProjectError)
    );
  }

  @Test
  void build_invalidPom() {
    Path pom = Paths.get("nope/pom.xml");
    assertThrows(BuildException.class, () -> maven.build(pom));
  }

  @Test
  void build_invalidGoal() {
    assertThrows(BuildException.class, () ->
      maven.build(validPom, List.of("nope"), MavenBuilder.DEFAULT_PROPERTIES)
    );
  }
}