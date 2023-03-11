package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.FIELD_NOW_STATIC;

class FieldNowStaticTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_NOW_STATIC, 0);
	}

	// Currently none, broken use of binary-incompatible changes still
	// need to be implemented
}
