package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
		Path c = Paths.get("../test-data/comp-changes/client/src/");
		Path sources = Paths.get("../test-data/comp-changes/old/");
		Path output = Paths.get("../test-data/comp-changes/client-commented/src");

		MaracasQuery query =
			new MaracasQuery.Builder()
				.v1(v1.toAbsolutePath())
				.v2(v2)
				.sources(sources)
				.output(output)
				.client(c)
				.build();

		MaracasResult result = new Maracas().analyze(query);
		System.out.println("Changes: " + result.delta());
		System.out.println("Impact: " + result.allDetections());
	}
}
