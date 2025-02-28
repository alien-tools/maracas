package com.github.maracas.forges.build.maven;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.Builder;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class MavenBuilder implements Builder {
	private final Path basePath;
	private final BuildConfig config;
	public static final String BUILD_FILE = "pom.xml";
	public static final List<String> DEFAULT_GOALS = List.of("package");
	protected static final Properties DEFAULT_PROPERTIES = new Properties();
	private static final Logger logger = LogManager.getLogger(MavenBuilder.class);

	// Skippable goals from https://maven.apache.org/plugins/
	static {
		DEFAULT_PROPERTIES.setProperty("maven.source.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.test.skip", "true");
		DEFAULT_PROPERTIES.setProperty("assembly.skipAssembly", "true");
		DEFAULT_PROPERTIES.setProperty("shade.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.war.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.rar.skip", "true");
		DEFAULT_PROPERTIES.setProperty("changelog.skip", "true");
		DEFAULT_PROPERTIES.setProperty("checkstyle.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.doap.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.javadoc.skip", "true");
		DEFAULT_PROPERTIES.setProperty("maven.jxr.skip", "true");
		DEFAULT_PROPERTIES.setProperty("linkcheck.skip", "true");
		DEFAULT_PROPERTIES.setProperty("pmd.skip", "true");
		DEFAULT_PROPERTIES.setProperty("mpir.skip", "true");
		DEFAULT_PROPERTIES.setProperty("gpg.skip", "true");
		DEFAULT_PROPERTIES.setProperty("jdepend.skip", "true");
	}

	public static boolean isMavenProject(Path basePath) {
		return Files.exists(basePath.resolve(BUILD_FILE));
	}

	public MavenBuilder(Path basePath, BuildConfig config) {
		this.basePath = Objects.requireNonNull(basePath);
		this.config = Objects.requireNonNull(config);
	}

	public MavenBuilder(Path basePath) {
		this(basePath, BuildConfig.newDefault());
	}

	@Override
	public void build(Duration timeout) {
		if (timeout.toSeconds() < 1)
			throw new IllegalArgumentException("timeout < 1s");

		File pomFile = basePath.resolve(BUILD_FILE).toFile();
		if (!pomFile.exists())
			throw new BuildException("Couldn't find pom.xml in %s".formatted(basePath));
		if (!basePath.resolve(config.getModule()).toFile().exists())
			throw new BuildException("Couldn't find module %s in %s".formatted(config.getModule(), basePath));

		Optional<Path> jar = locateJar();
		if (jar.isEmpty()) {
			List<String> goals = config.getGoals().isEmpty()
				? DEFAULT_GOALS
				: config.getGoals();
			Properties properties = config.getProperties().isEmpty()
				? DEFAULT_PROPERTIES
				: config.getProperties();

			logger.info("Building {} with module={} goals={}",
				pomFile, config.getModule(), goals);

			Stopwatch sw = Stopwatch.createStarted();
			StringBuilder errors = new StringBuilder();
			InvocationRequest request = new DefaultInvocationRequest();
			request.setPomFile(pomFile);
			request.setGoals(goals);
			request.setProperties(properties);
			request.setProjects(Collections.singletonList(config.getModule().toString()));
			request.setAlsoMake(true);
			request.setBatchMode(true);
			request.setQuiet(true);
			request.setTimeoutInSeconds(Math.toIntExact(timeout.toSeconds()));
			// For some reason, every handler but setOutputHandler is ignored
			// Here, invoked only with errors because quiet == true
			request.setOutputHandler(line -> {
				logger.error(line);
				errors.append(line);
			});

			try {
				Invoker invoker = new DefaultInvoker();
				InvocationResult result = invoker.execute(request);

				if (result.getExecutionException() != null)
					throw new BuildException("%s failed: %s".formatted(goals,
						result.getExecutionException().getCause() != null
							? result.getExecutionException().getCause().getMessage()
							: result.getExecutionException().getMessage()));
				if (result.getExitCode() != 0)
					throw new BuildException("%s failed (%d): %s".formatted(goals, result.getExitCode(), errors.toString()));

				logger.info("Building {} with module={} goals={} took {}ms",
					pomFile, config.getModule(), goals, sw.elapsed().toMillis());
			} catch (MavenInvocationException e) {
				throw new BuildException("Error invoking Maven", e);
			}
		} else logger.info("{} has already been built. Skipping.", pomFile);
	}

	@Override
	public Optional<Path> locateJar() {
		Path workingDirectory = basePath.resolve(config.getModule());
		Path target = workingDirectory.resolve("target");
		File pomFile = workingDirectory.resolve(BUILD_FILE).toFile();

		if (!Files.exists(target))
			return Optional.empty();

		MavenXpp3Reader reader = new MavenXpp3Reader();
		try (InputStream in = new FileInputStream(pomFile)) {
			Model model = reader.read(in);
			String aid = model.getArtifactId();
			String vid = !StringUtils.isEmpty(model.getVersion())
				? model.getVersion()
				: model.getParent().getVersion();
			Path jar = target.resolve("%s-%s.jar".formatted(aid, vid));

			if (Files.exists(jar))
				return Optional.of(jar);
			else {
				// There are cases that might fail us (e.g. <version>${revision}</version>)
				// => just attempt to find the best matching JAR, if any, avoiding -sources and -javadoc JARs
				return FileUtils.listFiles(target.toFile(), new WildcardFileFilter(String.format("%s-*.jar", aid)), null)
					.stream()
					.filter(f -> !f.getName().endsWith("-javadoc.jar") && !f.getName().endsWith("-sources.jar"))
					.map(File::toPath)
					.findFirst();
			}
		} catch (IOException | XmlPullParserException e) {
			throw new BuildException("Couldn't parse " + pomFile, e);
		}
	}

	@Override
	public List<BuildModule> locateModules() {
		List<BuildModule> modules = new ArrayList<>();

		try (Stream<Path> paths = Files.walk(basePath)) {
			paths
				.filter(f -> BUILD_FILE.equals(f.getFileName().toString()) && Files.isRegularFile(f))
				.forEach(pomFile -> {
					MavenXpp3Reader reader = new MavenXpp3Reader();
					try (InputStream in = new FileInputStream(pomFile.toFile())) {
						Model model = reader.read(in);
						String gid = StringUtils.isEmpty(model.getGroupId()) ? model.getParent().getGroupId() : model.getGroupId();
						String aid = model.getArtifactId();

						if (!StringUtils.isEmpty(gid) && !StringUtils.isEmpty(aid))
							modules.add(new BuildModule(String.format("%s:%s", gid, aid), basePath.relativize(pomFile.getParent())));
					} catch (IOException | XmlPullParserException e) {
						logger.error("Couldn't parse {}, skipping", pomFile);
					}
				});
		} catch (IOException e) {
			logger.error("Error walking directory {}", basePath, e);
		}

		return modules;
	}
}
