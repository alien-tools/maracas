package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.EXTENDS;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_ADDED;

import org.junit.jupiter.api.Test;

public class SuperclassAddedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(SUPERCLASS_ADDED, 6);
		// FIXME: Check other cases
	}

	@Test
	void testExtendsAbsClass() {
		assertDetection("SuperclassAddedExtAbs.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}

	@Test
	void testExtendsAbsClassMulti() {
		assertDetection("SuperclassAddedImp.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}

	@Test
	void testExtendsAbsClassMultiMulti() {
		assertDetection("SuperclassAddedImpMulti.java", 5, SUPERCLASS_ADDED, EXTENDS);
	}
}
