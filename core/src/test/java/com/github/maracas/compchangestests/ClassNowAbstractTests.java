package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.INSTANTIATION;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_ABSTRACT;

class ClassNowAbstractTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_NOW_ABSTRACT, 4);
	}

	@Test
	void testInstantiateAbstractClass() {
		assertBrokenUse("ClassNowAbstractMI.java", 8, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateAbstractClassParams() {
		assertBrokenUse("ClassNowAbstractMI.java", 12, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateInterface() {
		assertBrokenUse("IClassNowAbstractMI.java", 8, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateInterfaceParams() {
		assertBrokenUse("IClassNowAbstractMI.java", 12, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}
}
