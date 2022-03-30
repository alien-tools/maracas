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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenBuilderTest {
  final Path testProject = Paths.get("src/test/resources/maven-project/");
  final Path invalidProject = Paths.get("src/test/resources/");
  final Path validTarget = testProject.resolve("target/");
  final Path testProjectError = Paths.get("src/test/resources/maven-project-error/");

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(validTarget.toFile());
  }

  @Test
  void build_validPom_default() {
    MavenBuilder builder = new MavenBuilder(new BuildConfig(testProject));
    builder.build();
    System.out.println(builder.locateJar());
    assertTrue(builder.locateJar().isPresent());
  }

  @Test
  void build_validPom_withGoal() {
    BuildConfig configWithGoal = new BuildConfig(testProject);
    configWithGoal.addGoal("clean");
    MavenBuilder builder = new MavenBuilder(configWithGoal);
    builder.build();
    assertFalse(builder.locateJar().isPresent());
  }

  @Test
  void build_compileError() {
    MavenBuilder builder = new MavenBuilder(new BuildConfig(testProjectError));
    assertThrows(BuildException.class, builder::build);
  }

  @Test
  void build_invalidProject() {
    Builder builder = new MavenBuilder(new BuildConfig(invalidProject));
    assertThrows(BuildException.class, () -> builder.build());
  }

  @Test
  void build_invalidGoal() {
    BuildConfig configWithInvalidGoals = new BuildConfig(testProject);
    configWithInvalidGoals.addGoal("nope");
    MavenBuilder builder = new MavenBuilder(configWithInvalidGoals);
    assertThrows(BuildException.class, () ->
      builder.build()
    );
  }
}