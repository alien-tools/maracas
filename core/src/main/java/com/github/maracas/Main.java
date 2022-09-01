package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		Library v1 = new Library(
			Paths.get("test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			Paths.get("test-data/comp-changes/old/"));
		Library v2 = new Library(Paths.get("test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		Client c = new Client(Paths.get("test-data/comp-changes/client/"), v1);

		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(c)
			.build();

		AnalysisResult result = Maracas.analyze(query);
		System.out.println("Changes: " + result.delta());
		System.out.println("Impact:  " + result.allBrokenUses());
	}
}
