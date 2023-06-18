package com.github.maracas;

import java.nio.file.Path;

public class TestData {
	public static Path validJar = Path.of("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static Path invalidJar = Path.of("void.jar");
	public static Path validMavenDirectory = Path.of("./");
	public static Path validMavenDirectory2 = Path.of("../");
	public static Path invalidDirectory = Path.of("nope/");
	public static Path compChangesV1 = Path.of("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	public static Path compChangesV2 = Path.of("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
	public static Path compChangesSources = Path.of("../test-data/comp-changes/old/");
	public static Path compChangesClient = Path.of("../test-data/comp-changes/client/");
	public static LibraryJar validVersion = LibraryJar.withoutSources(validJar);
	public static SourcesDirectory validClient = SourcesDirectory.of(validMavenDirectory);
	public static SourcesDirectory validClient2 = SourcesDirectory.of(validMavenDirectory2);
}
