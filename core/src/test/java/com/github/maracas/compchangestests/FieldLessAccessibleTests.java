package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE;

class FieldLessAccessibleTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_LESS_ACCESSIBLE, 21);
	}

	@Test
	void testPub2Pro() {
		assertBrokenUse("FieldLessAccessibleFA.java", 14, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2PackPriv() {
		assertBrokenUse("FieldLessAccessibleFA.java", 18, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2Priv() {
		assertBrokenUse("FieldLessAccessibleFA.java", 22, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2Priv() {
		assertBrokenUse("FieldLessAccessibleFA.java", 26, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2Pro() {
		assertBrokenUse("FieldLessAccessibleFA.java", 30, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2PackPriv() {
		assertBrokenUse("FieldLessAccessibleFA.java", 34, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PackPrivNoSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 12, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PrivNoSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 16, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PackPrivNoSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 20, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PrivNoSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 24, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PackPrivSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 32, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PrivSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 36, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PackPrivSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 40, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PrivSuper() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 44, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPub2Priv() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 48, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPub2PackPriv() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 56, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPro2Priv() {
		assertBrokenUse("FieldLessAccessibleFASubtype.java", 60, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgPub2Priv() {
		assertBrokenUse("FieldLessAccessibleMI.java", 7, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgSuperPub2Priv() {
		assertBrokenUse("FieldLessAccessibleMI.java", 10, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgPackPriv2Priv() {
		assertBrokenUse("FieldLessAccessibleMI.java", 13, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgSuperPackPriv2Priv() {
		assertBrokenUse("FieldLessAccessibleMI.java", 16, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}
}
