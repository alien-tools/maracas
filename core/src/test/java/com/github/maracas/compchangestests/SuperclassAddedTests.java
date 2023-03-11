package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.compchangestests.CompChangesTest.*;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_ADDED;

class SuperclassAddedTests {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(SUPERCLASS_ADDED, 4);
		// FIXME: Check other cases
	}

	@Test
	void testExtendsAbsClass() {
		assertBrokenUse("SuperclassAddedExtAbs.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}

	@Test
	void testExtendsAbsClassMulti() {
		assertBrokenUse("SuperclassAddedImp.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}

	@Test
	void testExtendsAbsClassMultiMulti() {
		assertBrokenUse("SuperclassAddedImpMulti.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}

	@Disabled("Shadow objects. Missing information.")
	@Test
    void testExtendsAbsClassNoMethods() {
        assertNoBrokenUse("SuperclassAddedExt.java", 8, SUPERCLASS_ADDED, EXTENDS);
    }
}
