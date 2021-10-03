package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE;

import org.junit.jupiter.api.Test;

class FieldLessAccessibleTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_LESS_ACCESSIBLE, 21);
	}

	@Test
	void testPub2Pro() {
		assertDetection("FieldLessAccessibleFA.java", 14, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2PackPriv() {
		assertDetection("FieldLessAccessibleFA.java", 18, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2Priv() {
		assertDetection("FieldLessAccessibleFA.java", 22, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2Priv() {
		assertDetection("FieldLessAccessibleFA.java", 26, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2Pro() {
		assertDetection("FieldLessAccessibleFA.java", 30, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSuperPub2PackPriv() {
		assertDetection("FieldLessAccessibleFA.java", 34, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PackPrivNoSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 12, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PrivNoSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 16, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PackPrivNoSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 20, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PrivNoSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 24, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PackPrivSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 32, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePub2PrivSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 36, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PackPrivSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 40, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypePro2PrivSuper() {
		assertDetection("FieldLessAccessibleFASubtype.java", 44, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPub2Priv() {
		assertDetection("FieldLessAccessibleFASubtype.java", 48, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPub2PackPriv() {
		assertDetection("FieldLessAccessibleFASubtype.java", 56, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSubtypeSuperPro2Priv() {
		assertDetection("FieldLessAccessibleFASubtype.java", 60, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgPub2Priv() {
		assertDetection("FieldLessAccessibleMI.java", 7, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgSuperPub2Priv() {
		assertDetection("FieldLessAccessibleMI.java", 10, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgPackPriv2Priv() {
		assertDetection("FieldLessAccessibleMI.java", 13, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testSamePkgSuperPackPriv2Priv() {
		assertDetection("FieldLessAccessibleMI.java", 16, FIELD_LESS_ACCESSIBLE, FIELD_ACCESS);
	}
}
