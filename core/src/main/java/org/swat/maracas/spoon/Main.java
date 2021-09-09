package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.swat.maracas.spoon.delta.Detection;

public class Main {
	public static void main(String[] args) {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path c = Paths.get("/home/dig/repositories/comp-changes-data/client/src/");
		Path sources = Paths.get("/home/dig/repositories/comp-changes-data/old/");
		Path output = Paths.get("/home/dig/repositories/comp-changes-data/client-commented/src");

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
