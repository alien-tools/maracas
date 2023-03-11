package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.FIELD_NOW_FINAL;

class FieldNowFinalTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_NOW_FINAL, 6);
	}

	@Test
	void testExtSuper() {
		assertBrokenUse("FieldNowFinalExt.java", 8, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtNoSuper() {
		assertBrokenUse("FieldNowFinalExt.java", 17, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtSubSuper() {
		assertBrokenUse("FieldNowFinalExtSub.java", 8, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testExtSupNoSuper() {
		assertBrokenUse("FieldNowFinalExtSub.java", 17, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testFA() {
		assertBrokenUse("FieldNowFinalFA.java", 10, FIELD_NOW_FINAL, FIELD_ACCESS);
	}

	@Test
	void testFASub() {
		assertBrokenUse("FieldNowFinalFA.java", 21, FIELD_NOW_FINAL, FIELD_ACCESS);
	}
}
