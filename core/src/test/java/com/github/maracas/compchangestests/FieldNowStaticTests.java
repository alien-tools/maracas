package com.github.maracas.compchangestests;

import static japicmp.model.JApiCompatibilityChange.FIELD_NOW_STATIC;

import org.junit.jupiter.api.Test;

class FieldNowStaticTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_NOW_STATIC, 0);
	}

	// Currently none, detections of binary-incompatible changes still
	// need to be implemented
}
