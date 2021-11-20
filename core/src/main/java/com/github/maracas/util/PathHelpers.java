package com.github.maracas.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathHelpers {
	
	private PathHelpers() {}

	/**
	 * Checks whether {@code p} is a non-null {@link Path} to an existing JAR file
	 */
	public static boolean isValidJar(Path p) {
		return
			p != null &&
			Files.exists(p) &&
			Files.isRegularFile(p) &&
			p.getFileName().toString().endsWith(".jar");
	}

	/**
	 * Checks whether {@code p} is a non-null {@link Path} to an existing directory
	 */
	public static boolean isValidDirectory(Path p) {
		return
			p != null &&
			Files.exists(p) &&
			Files.isDirectory(p);
	}
}
