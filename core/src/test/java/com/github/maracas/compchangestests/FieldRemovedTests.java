package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_REMOVED;

import org.junit.jupiter.api.Test;

class FieldRemovedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_REMOVED, 16);
	}

	@Test
	void testAccessRemoved() {
		assertDetection("FieldRemovedFA.java", 10, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessRemovedStatic() {
		assertDetection("FieldRemovedFA.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuperRemoved() {
		assertDetection("FieldRemovedFA.java", 16, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuperRemovedStatic() {
		assertDetection("FieldRemovedFA.java", 18, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtAccessRemoved() {
		assertDetection("FieldRemovedExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtAccessRemovedSuper() {
		assertDetection("FieldRemovedExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtSubAccessRemoved() {
		assertDetection("FieldRemovedExtSub.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtSubAccessRemovedSuper() {
		assertDetection("FieldRemovedExtSub.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessIntfRemoved() {
		assertDetection("FieldRemovedImp.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessIntfRemovedQualified() {
		assertDetection("FieldRemovedImp.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	// FIELD_REMOVED detected because the containing classes are removed
	// or because it was removed from a super class
	@Test
	void testAccessFieldRemovedInSuper1() {
		assertDetection("FieldRemovedInSuperclassExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuper2() {
		assertDetection("FieldRemovedInSuperclassExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperSub1() {
		assertDetection("SFieldRemovedInSuperclassExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperSub2() {
		assertDetection("SFieldRemovedInSuperclassExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperFA1() {
		assertDetection("FieldRemovedInSuperclassFA.java", 10, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperFA2() {
		assertDetection("FieldRemovedInSuperclassFA.java", 15, FIELD_REMOVED, FIELD_ACCESS);
	}
}
