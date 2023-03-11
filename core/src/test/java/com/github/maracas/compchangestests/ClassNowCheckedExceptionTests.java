package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.THROWS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_CHECKED_EXCEPTION;

class ClassNowCheckedExceptionTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_NOW_CHECKED_EXCEPTION, 5);
	}

	@Test
	void testThrowsExcep() {
		assertBrokenUse("ClassNowCheckedExceptionThrows.java", 13, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsSubExcep() {
		assertBrokenUse("ClassNowCheckedExceptionThrows.java", 22, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsClientExcep() {
		assertBrokenUse("ClassNowCheckedExceptionThrows.java", 31, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsClientSubExcep() {
		assertBrokenUse("ClassNowCheckedExceptionThrows.java", 40, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsVariableException() {
		assertBrokenUse("ClassNowCheckedExceptionThrows.java", 50, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}
}
