package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestData {
	public static final Path validJar = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static final Path invalidJar = Paths.get("void.jar");
	public static final Path validDirectory = Paths.get("src/main/java");
	public static final Path validDirectory2 = Paths.get("src/test/java");
	public static final Path invalidDirectory = Paths.get("void/");
	public static final Path compChangesV1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static final Path compChangesV2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
	public static final Path compChangesSources = Paths.get("../test-data/comp-changes/old/src/");
	public static final Path compChangesClient = Paths.get("../test-data/comp-changes/client/src/");
}
