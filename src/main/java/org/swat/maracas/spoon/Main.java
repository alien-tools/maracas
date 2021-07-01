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

		MaracasAnalysis maracas = new MaracasAnalysis(v1, v2);
		maracas.computeDelta();
		maracas.computeDetections(c);
		Set<Detection> detections = maracas.getDetections();

		detections.forEach(d -> {
			if (d.change() == JApiCompatibilityChange.FIELD_REMOVED) {
				System.out.println(d);
			}
		});

		maracas.writeAnnotatedClient(output);
	}
}
