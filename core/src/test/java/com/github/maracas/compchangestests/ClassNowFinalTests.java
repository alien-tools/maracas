package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.EXTENDS;
import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_FINAL;

import org.junit.jupiter.api.Test;

class ClassNowFinalTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_NOW_FINAL, 9);
	}

	@Test
	void testExt() {
		assertDetection("ClassNowFinalExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testExtMethod() {
		assertDetection("ClassNowFinalExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testAbsExt() {
		assertDetection("ClassNowFinalAbsExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAbsExtMethod() {
		assertDetection("ClassNowFinalAbsExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testAbsExtSup() {
		assertDetection("ClassNowFinalAbsExtSup.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSub() {
		assertDetection("ClassNowFinalAnonymousSub.java", 8, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSubAbs() {
		assertDetection("ClassNowFinalAnonymousSub.java", 13, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSubAbsMethod() {
		assertDetection("ClassNowFinalAnonymousSub.java", 15, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	// A bit of a corner case: a type goes from enum to class; this is reported
	// as a CLASS_NOW_FINAL since subclasses cannot extend the type anymore
	@Test
	void testExtendAnEnum() {
		assertDetection("ClassTypeChangedC2EExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}
}
