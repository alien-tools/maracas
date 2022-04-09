package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.AbstractBuilder;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class MavenBuilder extends AbstractBuilder {
  public static final String BUILD_FILE = "pom.xml";
  public static final List<String> DEFAULT_GOALS = List.of("package");
  public static final Properties DEFAULT_PROPERTIES = new Properties();
  private static final Logger logger = LogManager.getLogger(MavenBuilder.class);

  static {
    DEFAULT_PROPERTIES.setProperty("maven.test.skip", "true");
    DEFAULT_PROPERTIES.setProperty("assembly.skipAssembly", "true");
  }

  public MavenBuilder(BuildConfig config) {
    super(config);
  }

  @Override
  public void build() {
    File pomFile = config.getBasePath().resolve(BUILD_FILE).toFile();
    Optional<Path> jar = locateJar();

    if (jar.isEmpty()) {
      List<String> goals = config.getGoals().isEmpty()
        ? DEFAULT_GOALS
        : config.getGoals();
      Properties properties = config.getProperties().isEmpty()
        ? DEFAULT_PROPERTIES
        : config.getProperties();

      InvocationRequest request = new DefaultInvocationRequest();
      request.setPomFile(pomFile);
      request.setGoals(goals);
      request.setProperties(properties);
      request.setBatchMode(true);

      try {
        logger.info("Building {} with goals={} properties={}",
          pomFile.getAbsolutePath(), goals, properties);
        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);

        if (result.getExecutionException() != null)
          throw new BuildException(goals + " failed: " + result.getExecutionException().getMessage());
        if (result.getExitCode() != 0)
          throw new BuildException(goals + " failed: " + result.getExitCode());
      } catch (MavenInvocationException e) {
        throw new BuildException("Maven build error", e);
      }
    } else logger.info("{} has already been built. Skipping.", pomFile);
  }

  @Override
  public Optional<Path> locateJar() {
    File pomFile = config.getBasePath().resolve(BUILD_FILE).toFile();
    MavenXpp3Reader reader = new MavenXpp3Reader();
    try (InputStream in = new FileInputStream(pomFile)) {
      Model model = reader.read(in);
      String aid = model.getArtifactId();
      String vid = model.getVersion();
      Path jar = config.getBasePath().resolve("target").resolve("%s-%s.jar".formatted(aid, vid));

      if (Files.exists(jar))
        return Optional.of(jar);
      else
        return Optional.empty();
    } catch (IOException | XmlPullParserException e) {
      throw new BuildException("Couldn't parse " + pomFile, e);
    }
  }
}
