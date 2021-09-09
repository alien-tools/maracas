package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.github.maracas.delta.Detection;

public class Main {
	public static void main(String[] args) {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/old-0.0.1-SNAPSHOT.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/new-0.0.1-SNAPSHOT.jar");
		Path c = Paths.get("../test-data/comp-changes/client/src/");
		Path sources = Paths.get("../test-data/comp-changes/old/");
		Path output = Paths.get("../test-data/comp-changes/client-commented/src");

		VersionAnalyzer version = new VersionAnalyzer(v1, v2);
		version.computeDelta();
		version.populateLocations(sources);
		ClientAnalyzer analyzer = version.analyzeClient(c);
		Set<Detection> detections = version.getDetections();

		detections.forEach(d -> {
			System.out.println(d);
		});

		analyzer.writeAnnotatedClient(output);
	}
}
