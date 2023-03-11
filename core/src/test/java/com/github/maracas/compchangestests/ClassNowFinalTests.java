package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_FINAL;

class ClassNowFinalTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_NOW_FINAL, 6);
	}

	@Test
	void testExt() {
		assertBrokenUse("ClassNowFinalExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAbsExt() {
		assertBrokenUse("ClassNowFinalAbsExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAbsExtSup() {
		assertBrokenUse("ClassNowFinalAbsExtSup.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSub() {
		assertBrokenUse("ClassNowFinalAnonymousSub.java", 8, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAnonymousSubAbs() {
		assertBrokenUse("ClassNowFinalAnonymousSub.java", 13, CLASS_NOW_FINAL, EXTENDS);
	}

	// A bit of a corner case: a type goes from enum to class; this is reported
	// as a CLASS_NOW_FINAL since subclasses cannot extend the type anymore
	@Test
	void testExtendAnEnum() {
		assertBrokenUse("ClassTypeChangedC2EExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}
}
