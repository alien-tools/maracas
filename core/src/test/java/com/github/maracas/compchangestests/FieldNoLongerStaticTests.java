package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_NO_LONGER_STATIC;

import org.junit.jupiter.api.Test;

class FieldNoLongerStaticTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_NO_LONGER_STATIC, 5);
	}

	@Test
	void testFAClient() {
		assertBrokenUse("FieldNoLongerStaticFA.java", 9, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	// Fails when accessing a super static field through a subtype
	// (FieldNoLongerStatic.superFieldStatic)
	@Test
	void testFAClientSuper1() {
		assertBrokenUse("FieldNoLongerStaticFA.java", 13, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testFAClientSuper2() {
		assertBrokenUse("FieldNoLongerStaticFA.java", 17, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testExtClientStatic() {
		assertBrokenUse("FieldNoLongerStaticExt.java", 17, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}

	@Test
	void testExtClientSuperStatic() {
		assertBrokenUse("FieldNoLongerStaticExt.java", 29, FIELD_NO_LONGER_STATIC, FIELD_ACCESS);
	}
}
