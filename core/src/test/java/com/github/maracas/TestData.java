package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestData {
	public static Path validJar = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static Path invalidJar = Paths.get("void.jar");
	public static Path validMavenDirectory = Paths.get("./");
	public static Path validMavenDirectory2 = Paths.get("../");
	public static Path invalidDirectory = Paths.get("nope/");
	public static Path compChangesV1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static Path compChangesV2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
	public static Path compChangesSources = Paths.get("../test-data/comp-changes/old/");
	public static Path compChangesClient = Paths.get("../test-data/comp-changes/client/");
	public static Library validVersion = new Library(validJar);
	public static Client validClient = new Client(validMavenDirectory, validVersion);
	public static Client validClient2 = new Client(validMavenDirectory2, validVersion);
}
