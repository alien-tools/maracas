package com.github.maracas;

import java.nio.file.Path;

public class Main {
	public static void main(String[] args) {
		LibraryJar v1 = LibraryJar.withSources(
			Path.of("test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			SourcesDirectory.of(Path.of("test-data/comp-changes/old/")));
		LibraryJar v2 = LibraryJar.withoutSources(Path.of("test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		SourcesDirectory client = SourcesDirectory.of(Path.of("test-data/comp-changes/client/"));

		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(client)
			.build();

		AnalysisResult result = Maracas.analyze(query);
		System.out.println("Changes: " + result.delta());
		System.out.println("Impact:  " + result.allBrokenUses());
	}
}
