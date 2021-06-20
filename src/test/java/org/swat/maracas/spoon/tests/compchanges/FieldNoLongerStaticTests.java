package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.FIELD_NO_LONGER_STATIC;
import static org.swat.maracas.spoon.APIUse.FIELD_ACCESS;

import org.junit.jupiter.api.Test;

class FieldNoLongerStaticTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_NO_LONGER_STATIC, 5);
	}

	@Test
	void testFAClient() {
		assertDetection("FieldNoLongerStaticFA.java", 9, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	// Fails when accessing a super static field through a subtype
	// (FieldNoLongerStatic.superFieldStatic)
	@Test
	void testFAClientSuper1() {
		assertDetection("FieldNoLongerStaticFA.java", 13, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testFAClientSuper2() {
		assertDetection("FieldNoLongerStaticFA.java", 17, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testExtClientStatic() {
		assertDetection("FieldNoLongerStaticExt.java", 17, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testExtClientSuperStatic() {
		assertDetection("FieldNoLongerStaticExt.java", 29, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}
}
