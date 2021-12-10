package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.EXTENDS;
import static com.github.maracas.brokenUse.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_FINAL;

import org.junit.jupiter.api.Test;

class ClassNowFinalTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_NOW_FINAL, 9);
	}

	@Test
	void testExt() {
		assertBrokenUse("ClassNowFinalExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testExtMethod() {
		assertBrokenUse("ClassNowFinalExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testAbsExt() {
		assertBrokenUse("ClassNowFinalAbsExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}

	@Test
	void testAbsExtMethod() {
		assertBrokenUse("ClassNowFinalAbsExt.java", 8, CLASS_NOW_FINAL, METHOD_OVERRIDE);
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

	@Test
	void testAnonymousSubAbsMethod() {
		assertBrokenUse("ClassNowFinalAnonymousSub.java", 15, CLASS_NOW_FINAL, METHOD_OVERRIDE);
	}

	// A bit of a corner case: a type goes from enum to class; this is reported
	// as a CLASS_NOW_FINAL since subclasses cannot extend the type anymore
	@Test
	void testExtendAnEnum() {
		assertBrokenUse("ClassTypeChangedC2EExt.java", 5, CLASS_NOW_FINAL, EXTENDS);
	}
}
