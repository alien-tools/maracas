package com.github.maracas.tests.compchanges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;

import com.github.maracas.SpoonHelper;
import com.github.maracas.VersionAnalyzer;
import com.github.maracas.delta.APIUse;
import com.github.maracas.delta.Detection;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

public class CompChangesTest {
	static Set<Detection> detections;

	@BeforeAll
	static void setUp() {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/old-0.0.1-SNAPSHOT.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/new-0.0.1-SNAPSHOT.jar");
		Path client = Paths.get("../test-data/comp-changes/client/src/");

		VersionAnalyzer version = new VersionAnalyzer(v1, v2);
		version.computeDelta();
		version.analyzeClient(client);
		detections = version.getDetections();
	}

	public static void assertDetection(String file, int line, JApiCompatibilityChange change, APIUse use) {
		Optional<Detection> find =
			detections.stream().filter(d -> {
				if (change != d.change())
					return false;
				if (use != d.use())
					return false;

				SourcePosition pos = SpoonHelper.firstLocatableParent(d.element()).getPosition();
				if (pos instanceof NoSourcePosition)
					return false;
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
