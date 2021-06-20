package org.swat.maracas.spoon.tests.compchanges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.swat.maracas.spoon.APIUse;
import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.Maracas;
import org.swat.maracas.spoon.SpoonHelper;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.SourcePosition;

public class CompChangesTest {
	static Set<Detection> detections;

	@BeforeAll
	static void setUp() {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path client = Paths.get("/home/dig/repositories/comp-changes-data/client/src/");
		Maracas maracas = new Maracas(v1, v2, client);

		maracas.computeDelta();
		detections = maracas.computeDetections();
	}

	public static void assertDetection(String file, int line, JApiCompatibilityChange change, APIUse use) {
		Optional<Detection> find =
			detections.stream().filter(d -> {
				if (change != d.change())
					return false;
				if (use != d.use())
					return false;

				SourcePosition pos = SpoonHelper.firstLocatableParent(d.element()).getPosition();
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
		List<Detection> ds = detections.stream().filter(d -> d.change() == change).collect(Collectors.toList());

		assertEquals(n, ds.size(), ds.toString());
	}
}