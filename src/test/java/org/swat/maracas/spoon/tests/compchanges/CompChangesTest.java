package org.swat.maracas.spoon.tests.compchanges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.swat.maracas.spoon.Delta;
import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.Detection.APIUse;
import org.swat.maracas.spoon.visitors.ImpactVisitor;

import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.output.Filter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.cu.SourcePosition;

public class CompChangesTest {
	static Set<Detection> detections;

	@BeforeAll
	static void setUp() {
		Path v1 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-new/target/comp-changes-0.0.2.jar");
		Path client = Paths.get("/home/dig/repositories/comp-changes-client/src/");

		detections = computeDetections(v1, v2, client);
	}

	public static Set<Detection> computeDetections(Path v1, Path v2, Path client) {
		Delta delta = new Delta();
		List<JApiClass> classes = delta.compute(v1, v2, Collections.emptyList(), Collections.emptyList());

		Launcher launcher = new Launcher();
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] cp = {v1.toAbsolutePath().toString()};
		launcher.getEnvironment().setSourceClasspath(cp);
		CtModel model = launcher.buildModel();

		ImpactVisitor visitor = new ImpactVisitor(model.getRootPackage());
		Filter.filter(classes, visitor);

		return visitor.getDetections();
	}

	public static void assertDetection(String file, int line, JApiCompatibilityChange change, APIUse use) {
		Optional<Detection> find =
			detections.stream().filter(d -> {
				if (change != d.getChange())
					return false;
				if (use != d.getUse())
					return false;

				SourcePosition pos = d.getElement().getPosition();
				if (!file.equals(pos.getFile().getName().toString()))
					return false;
				if (line != pos.getLine())
					return false;
				
				return true;
			}).findAny();

		assertTrue(
			find.isPresent(),
			String.format("No detection found in %s:%d [%s] [%s]",
				file, line, change, use)
		);
	}

	public static void assertNumberDetections(JApiCompatibilityChange change, int n) {
		List<Detection> ds = detections.stream().filter(d -> d.getChange() == change).collect(Collectors.toList());

		assertEquals(n, ds.size(), ds.toString());
	}
}
