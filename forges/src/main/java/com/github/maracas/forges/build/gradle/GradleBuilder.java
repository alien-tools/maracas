package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.clone.CloneException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GradleBuilder implements Builder {
  private final Path gradle;
  private final Path base;

  public GradleBuilder(Path gradle) {
    Objects.requireNonNull(gradle);
    if (!gradle.toFile().exists())
      throw new BuildException("The gradlew file doesn't exist: " + gradle);

    this.gradle = gradle.toAbsolutePath();
    this.base = gradle.getParent().toAbsolutePath();
  }

  @Override
  public void build() throws BuildException {
    executeCommand(getCommand());
  }

  @Override
  public void build(List<String> goals, Properties properties) throws BuildException {
    if (goals.isEmpty())
      throw new BuildException("No provided goal.");

    executeCommand(getCommand(goals, properties));
  }

  @Override
  public Optional<Path> locateJar() {
    return getFile(Paths.get("build", "libs", base.getFileName().toString() + ".jar"));
  }

  @Override
  public Path getBasePath() {
    return base;
  }

  private static String[] getCommand() {
    List<String> defaultGoals = Arrays.asList("build");
    Properties defaultProps = new Properties();
    defaultProps.setProperty("-x", "test");
    return getCommand(defaultGoals, defaultProps);
  }

  private static String[] getCommand(List<String> goals, Properties properties) {
    List<String> commands = new ArrayList<>();
    commands.add("./gradlew");
    commands.addAll(goals);
    for (String property: properties.stringPropertyNames()) {
      String value = properties.getProperty(property);
      if ("".equals(value))
        commands.add(property);
      else {
        commands.add(property);
        commands.add(value);
      }
    }
    String[] finalCommand = new String[commands.size()];
    commands.toArray(finalCommand);
    return finalCommand;
  }

  private void executeCommand(String... command) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(this.base.toFile());
      int exitCode = pb.start().waitFor();
      if (exitCode != 0)
        throw new BuildException("%s failed: %d".formatted(Arrays.asList(command), exitCode));
    } catch (IOException e) {
      throw new BuildException("Gradle failed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
