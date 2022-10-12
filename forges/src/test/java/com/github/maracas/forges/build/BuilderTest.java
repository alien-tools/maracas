package com.github.maracas.forges.build;

import com.github.maracas.forges.build.gradle.GradleBuilder;
import com.github.maracas.forges.build.maven.MavenBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

class BuilderTest {
  final Path mavenProject = Path.of("src/test/resources/maven-project/");
  final Path gradleProject = Path.of("src/test/resources/gradle-project/");
  final Path invalidProject = Path.of("src/test/resources/");

  @Test
  void build_From_MavenProject() {
    assertThat(Builder.of(mavenProject, BuildConfig.newDefault()), instanceOf(MavenBuilder.class));
  }

  @Test
  void build_From_GradleProject() {
    assertThat(Builder.of(gradleProject, BuildConfig.newDefault()), instanceOf(GradleBuilder.class));
  }

  @Test
  void build_From_UnknownProject() {
    BuildConfig defaultConfig = BuildConfig.newDefault();
    assertThrows(BuildException.class, () -> Builder.of(invalidProject, defaultConfig));
  }
}
