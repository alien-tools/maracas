package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_CHECKED_EXCEPTION;
import static org.swat.maracas.spoon.APIUse.THROWS;

import org.junit.jupiter.api.Test;

class ClassNowCheckedExceptionTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_NOW_CHECKED_EXCEPTION, 5);
	}

	@Test
	void testThrowsExcep() {
		assertDetection("ClassNowCheckedExceptionThrows.java", 13, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsSubExcep() {
		assertDetection("ClassNowCheckedExceptionThrows.java", 22, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsClientExcep() {
		assertDetection("ClassNowCheckedExceptionThrows.java", 31, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}

	@Test
	void testThrowsClientSubExcep() {
		assertDetection("ClassNowCheckedExceptionThrows.java", 40, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}
	
	@Test
	void testThrowsVariableException() {
		assertDetection("ClassNowCheckedExceptionThrows.java", 50, CLASS_NOW_CHECKED_EXCEPTION, THROWS);
	}
}
