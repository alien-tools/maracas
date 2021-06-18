package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_FINAL;
import static org.swat.maracas.spoon.Detection.APIUse.EXTENDS;
import static org.swat.maracas.spoon.Detection.APIUse.METHOD_OVERRIDE;

import org.junit.jupiter.api.Test;

class ClassNowFinalTests extends CompChangesTest {
	// TODO: japicmp reports a CLASS_NOW_FINAL when a class goes from Class to Enum.
	//       Weird behavior => fix that upstream?
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_NOW_FINAL, 8);
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
}
