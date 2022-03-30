package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
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
  final Path invalidProject = Paths.get("src/test/resources/");
  final Path validTarget = testProject.resolve("build/");

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(validTarget.toFile());
  }

  @Test
  void build_validGradlew_default() {
    GradleBuilder builder = new GradleBuilder(new BuildConfig(testProject));
    builder.build();
    assertTrue(builder.locateJar().isPresent());
  }

  @Test
  void build_validGradlew_withGoal() {
    BuildConfig configWithGoal = new BuildConfig(testProject);
    configWithGoal.addGoal("clean");
    GradleBuilder builder = new GradleBuilder(configWithGoal);
    builder.build();
    assertFalse(builder.locateJar().isPresent());
  }

  @Test
  void build_invalidProject() {
    Builder builder = new GradleBuilder(new BuildConfig(invalidProject));
    assertThrows(BuildException.class, () -> builder.build());
  }

  @Test
  void build_validGradlew_invalidGoal() {
    BuildConfig configWithInvalidGoal = new BuildConfig(testProject);
    configWithInvalidGoal.addGoal("nope");
    GradleBuilder builder = new GradleBuilder(configWithInvalidGoal);
    assertThrows(BuildException.class, () ->
      builder.build()
    );
  }
}