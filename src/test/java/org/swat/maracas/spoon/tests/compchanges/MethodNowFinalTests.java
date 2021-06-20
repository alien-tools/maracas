package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.*;
import static org.swat.maracas.spoon.APIUse.*;

import org.junit.jupiter.api.Test;

class MethodNowFinalTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_NOW_FINAL, 3);
	}

	@Test
	void testExt() {
		assertDetection("MethodNowFinalExt.java", 8, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testExtMethod() {
		assertDetection("MethodNowFinalExt.java", 13, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testExtMethodNoOverride() {
		assertDetection("MethodNowFinalExt.java", 21, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}
}
