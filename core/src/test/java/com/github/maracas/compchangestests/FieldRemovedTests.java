package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_REMOVED;

import org.junit.jupiter.api.Test;

class FieldRemovedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_REMOVED, 16);
	}

	@Test
	void testAccessRemoved() {
		assertBrokenUse("FieldRemovedFA.java", 10, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessRemovedStatic() {
		assertBrokenUse("FieldRemovedFA.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuperRemoved() {
		assertBrokenUse("FieldRemovedFA.java", 16, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuperRemovedStatic() {
		assertBrokenUse("FieldRemovedFA.java", 18, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtAccessRemoved() {
		assertBrokenUse("FieldRemovedExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtAccessRemovedSuper() {
		assertBrokenUse("FieldRemovedExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtSubAccessRemoved() {
		assertBrokenUse("FieldRemovedExtSub.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtSubAccessRemovedSuper() {
		assertBrokenUse("FieldRemovedExtSub.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessIntfRemoved() {
		assertBrokenUse("FieldRemovedImp.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessIntfRemovedQualified() {
		assertBrokenUse("FieldRemovedImp.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	// FIELD_REMOVED detected because the containing classes are removed
	// or because it was removed from a super class
	@Test
	void testAccessFieldRemovedInSuper1() {
		assertBrokenUse("FieldRemovedInSuperclassExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuper2() {
		assertBrokenUse("FieldRemovedInSuperclassExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperSub1() {
		assertBrokenUse("SFieldRemovedInSuperclassExt.java", 8, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperSub2() {
		assertBrokenUse("SFieldRemovedInSuperclassExt.java", 12, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperFA1() {
		assertBrokenUse("FieldRemovedInSuperclassFA.java", 10, FIELD_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testAccessFieldRemovedInSuperFA2() {
		assertBrokenUse("FieldRemovedInSuperclassFA.java", 15, FIELD_REMOVED, FIELD_ACCESS);
	}
}
