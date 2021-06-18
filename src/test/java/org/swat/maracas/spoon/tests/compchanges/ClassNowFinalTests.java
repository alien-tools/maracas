package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_FINAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.swat.maracas.spoon.Detection.APIUse.EXTENDS;
import static org.swat.maracas.spoon.Detection.APIUse.METHOD_OVERRIDE;
import static org.swat.maracas.spoon.tests.compchanges.DetectionAssertions.assertDetection;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.swat.maracas.spoon.Detection;

class ClassNowFinalTests {
	static Set<Detection> detections;

	@BeforeAll
	static void setUp() {
		detections = CompChanges.computeDetections();
	}

	// TODO: japicmp reports a CLASS_NOW_FINAL when a class goes from
	//       Class to Enum. Weird behavior, fix that upstream?
	@Test
	void testNoMore() {
		System.out.println(detections.stream().filter(d -> d.getChange() == CLASS_NOW_FINAL).collect(Collectors.toList()));
		assertEquals(
			8,
			detections.stream().filter(d -> d.getChange() == CLASS_NOW_FINAL).count());
	}

	@Test
	void testExt() {
		assertDetection(detections, "ClassNowFinalExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testExtMethod() {
		assertDetection(detections, "ClassNowFinalExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testAbsExt() {
		assertDetection(detections, "ClassNowFinalAbsExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAbsExtMethod() {
		assertDetection(detections, "ClassNowFinalAbsExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}
	
	@Test
	void testAbsExtSup() {
		assertDetection(detections, "ClassNowFinalAbsExtSup.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}
	
	@Test
	void testAnonymousSub() {
		assertDetection(detections, "ClassNowFinalAnonymousSub.java", 8, CLASS_NOW_FINAL, EXTENDS);
	}
	
	@Test
	void testAnonymousSubAbs() {
		assertDetection(detections, "ClassNowFinalAnonymousSub.java", 13, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSubAbsMethod() {
		assertDetection(detections, "ClassNowFinalAnonymousSub.java", 15, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}
}
