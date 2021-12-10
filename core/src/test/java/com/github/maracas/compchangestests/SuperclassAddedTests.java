package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.EXTENDS;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_ADDED;

import org.junit.jupiter.api.Test;

public class SuperclassAddedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(SUPERCLASS_ADDED, 6);
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
}
