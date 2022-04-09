package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.METHOD_NOW_FINAL;

import org.junit.jupiter.api.Test;

class MethodNowFinalTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(METHOD_NOW_FINAL, 3);
	}

	@Test
	void testExt() {
		assertBrokenUse("MethodNowFinalExt.java", 8, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testExtMethod() {
		assertBrokenUse("MethodNowFinalExt.java", 13, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}

	@Test
	void testExtMethodNoOverride() {
		assertBrokenUse("MethodNowFinalExt.java", 21, METHOD_NOW_FINAL, METHOD_OVERRIDE);
	}
}
