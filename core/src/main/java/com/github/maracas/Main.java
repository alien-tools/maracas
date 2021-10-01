package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
		Path c = Paths.get("../test-data/comp-changes/client/src/");
		Path sources = Paths.get("../test-data/comp-changes/old/");

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(v1)
			.newJar(v2)
			.sources(sources)
			.client(c)
			.build();

		AnalysisResult result = new Maracas().analyze(query);
		System.out.println("Changes: " + result.delta());
		System.out.println("Impact:  " + result.allDetections());
	}
}
