package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.THROWS;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_CHECKED_EXCEPTION;

import org.junit.jupiter.api.Test;

class ClassNowCheckedExceptionTests extends CompChangesTest {
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
