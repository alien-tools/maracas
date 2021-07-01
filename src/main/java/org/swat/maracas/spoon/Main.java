package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import japicmp.model.JApiCompatibilityChange;

public class Main {
	public static void main(String[] args) {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path c = Paths.get("/home/dig/repositories/comp-changes-data/client/src/");
		Path output = Paths.get("/home/dig/repositories/comp-changes-data/client-commented/src");

		VersionAnalyzer version = new VersionAnalyzer(v1, v2);
		version.computeDelta();
		ClientAnalyzer analyzer = version.analyzeClient(c);
		Set<Detection> detections = version.getDetections();

		detections.forEach(d -> {
			if (d.change() == JApiCompatibilityChange.FIELD_REMOVED) {
				System.out.println(d);
			}
		});

		analyzer.writeAnnotatedClient(output);
	}
}
