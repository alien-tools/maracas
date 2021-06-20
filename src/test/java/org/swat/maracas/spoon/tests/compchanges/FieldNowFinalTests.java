package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.FIELD_NOW_FINAL;
import static org.swat.maracas.spoon.APIUse.FIELD_ACCESS;

import org.junit.jupiter.api.Test;

class FieldNowFinalTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_NOW_FINAL, 6);
	}

	@Test
	void testExtSuper() {
		assertDetection("FieldNowFinalExt.java", 8, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtNoSuper() {
		assertDetection("FieldNowFinalExt.java", 17, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtSubSuper() {
		assertDetection("FieldNowFinalExtSub.java", 8, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtSupNoSuper() {
		assertDetection("FieldNowFinalExtSub.java", 17, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testFA() {
		assertDetection("FieldNowFinalFA.java", 10, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testFASub() {
		assertDetection("FieldNowFinalFA.java", 21, FIELD_NOW_FINAL, FIELD_ACCESS);
	}
}
