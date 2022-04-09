package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.AbstractBuilder;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GradleBuilder extends AbstractBuilder {
  public static final String BUILD_FILE = "gradlew";
  public static final List<String> DEFAULT_TASKS = List.of("build");
  public static final Properties DEFAULT_PROPERTIES = new Properties();
  private static final Logger logger = LogManager.getLogger(GradleBuilder.class);

  static {
    DEFAULT_PROPERTIES.setProperty("-x", "test");
  }

  public GradleBuilder(BuildConfig config) {
    super(config);
  }

  @Override
  public void build() {
    File gradlewFile = config.getBasePath().resolve(BUILD_FILE).toFile();
    Optional<Path> jar = locateJar();

    if (jar.isEmpty()) {
      List<String> goals = config.getGoals().isEmpty()
        ? DEFAULT_TASKS
        : config.getGoals();
      Properties properties = config.getProperties().isEmpty()
        ? DEFAULT_PROPERTIES
        : config.getProperties();

      executeCommand(getCommand(goals, properties));
    } else logger.info("{} has already been built. Skipping.", gradlewFile);
  }

  @Override
  public Optional<Path> locateJar() {
    Path jar = config.getBasePath()
      .resolve("build")
      .resolve("libs")
      .resolve(config.getBasePath().getFileName().toString() + ".jar");

    if (Files.exists(jar))
      return Optional.of(jar);
    else
      return Optional.empty();
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
      pb.directory(config.getBasePath().toFile());
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
