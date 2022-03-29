package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.maven.MavenBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class GradleBuilderTest {
  final Path testProject = Paths.get("src/test/resources/gradle-project/");
  final Path validGradlew = testProject.resolve("gradlew");
  final Path validTarget = testProject.resolve("build/");

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(validTarget.toFile());
  }

  @Test
  void build_validGradlew_default() {
    GradleBuilder builder = new GradleBuilder(validGradlew);
    builder.build();
    assertTrue(builder.getFile(Paths.get("build/libs/gradle-project.jar")).isPresent());
    assertTrue(builder.locateJar().isPresent());
  }

  @Test
  void build_validGradlew_withGoal() {
    GradleBuilder builder = new GradleBuilder(validGradlew);
    builder.build(List.of("clean"), new Properties());
    assertFalse(builder.locateJar().isPresent());
  }


  @Test
  void build_invalidPom() {
    Path pom = Paths.get("nope/gradlew");
    assertThrows(BuildException.class, () -> new MavenBuilder(pom));
  }

  @Test
  void build_invalidGoal() {
    GradleBuilder builder = new GradleBuilder(validGradlew);
    assertThrows(BuildException.class, () ->
      builder.build(List.of("nope"), new Properties())
    );
  }

  @Test
  void build_noGoal() {
    GradleBuilder builder = new GradleBuilder(validGradlew);
    assertThrows(BuildException.class, () ->
      builder.build(Collections.emptyList(), new Properties())
    );
  }
}