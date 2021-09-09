package com.github.maracas.tests.compchanges;

import static com.github.maracas.delta.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_REMOVED;

import org.junit.jupiter.api.Test;

class FieldRemovedTests extends CompChangesTest {
	// We're just checking the cases related to FIELD_REMOVED directly,
	// not those caused by other removals
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_REMOVED, 10);
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
}
