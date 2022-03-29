package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_NOW_FINAL;

import org.junit.jupiter.api.Test;

class FieldNowFinalTests extends CompChangesTest {
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
