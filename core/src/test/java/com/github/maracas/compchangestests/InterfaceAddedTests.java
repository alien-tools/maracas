package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.brokenuse.APIUse.IMPLEMENTS;
import static com.github.maracas.compchangestests.CompChangesTest.*;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_ADDED;

class InterfaceAddedTests {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(INTERFACE_ADDED, 7);
		// FIXME: Check other cases
	}

	@Test
	void testExtendsAbsClass() {
		assertBrokenUse("InterfaceAddedExtAbs.java", 5, INTERFACE_ADDED, EXTENDS);
	}

	@Test
	void testImplementsInter() {
		assertBrokenUse("InterfaceAddedImp.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}

	@Disabled("Fix!")
	@Test
	void testImplementsMultiInter() {
		assertBrokenUse("InterfaceAddedImpMulti.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}

	@Disabled("Shadow objects. Missing information.")
    @Test
    void testExtendsAbsClassNoMethods() {
        assertNoBrokenUse("InterfaceAddedExt.java", 8, INTERFACE_ADDED, EXTENDS);
    }
}
