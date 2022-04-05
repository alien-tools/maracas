package com.github.maracas.forges.build.gradle;

import com.github.maracas.forges.build.AbstractBuilder;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GradleBuilder extends AbstractBuilder {
	public static final String BUILD_FILE = "gradlew";
	public static final String DEFAULT_ARGS = "build -x test";
	private static final Logger logger = LogManager.getLogger(GradleBuilder.class);

	public GradleBuilder(BuildConfig config) {
		super(config);
	}

	@Override
	public void build() {
		File gradlewFile = config.basePath().resolve(BUILD_FILE).toFile();

		if (!gradlewFile.exists())
			throw new BuildException("{} doesn't exist".formatted(gradlewFile));

		Optional<Path> jar = locateJar();
		if (jar.isEmpty()) {
			// FIXME: This is perfectly secure /shrug
			String[] args = !StringUtils.isEmpty(config.args())
				? config.args().split(" ")
				: DEFAULT_ARGS.split(" ");
			String[] command = ArrayUtils.insert(0, args, gradlewFile.getAbsolutePath());
			executeCommand(command);
		} else logger.info("{} has already been built. Skipping.", gradlewFile);
	}

//	@Override
//	public Optional<Path> locateJar() {
//		Path jar = config.basePath()
//			.resolve("build")
//			.resolve("libs")
//			.resolve(config.basePath().getFileName().toString() + ".jar");
//
//		if (Files.exists(jar))
//			return Optional.of(jar);
//		else
//			return Optional.empty();
//	}
}
