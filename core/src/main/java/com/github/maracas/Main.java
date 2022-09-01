package com.github.maracas;

import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		LibraryJar v1 = new LibraryJar(
			Paths.get("test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			new SourcesDirectory(Paths.get("test-data/comp-changes/old/")));
		LibraryJar v2 = new LibraryJar(Paths.get("test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		SourcesDirectory client = new SourcesDirectory(Paths.get("test-data/comp-changes/client/"));

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
