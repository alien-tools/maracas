package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.IMPLEMENTS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.METHOD_ADDED_TO_INTERFACE;

class MethodAddedToInterfaceTests {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(METHOD_ADDED_TO_INTERFACE, 4);
	}

	@Test
	void testClassImpl1() {
		assertBrokenUse("MethodAddedToInterfaceImp1.java", 5, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}

	@Test
	void testClassImpl2() {
		assertBrokenUse("MethodAddedToInterfaceImp2.java", 5, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}

	@Test
	void testInnerClassImpl1() {
		assertBrokenUse("MethodAddedToInterfaceInnerImp.java", 7, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}

	@Test
	void testInnerClassImpl2() {
		assertBrokenUse("MethodAddedToInterfaceInnerExt.java", 7, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}

//	@Test
//	void testClassImplTrans() {
//		assertBrokenUse("MethodAddedToInterfaceTransImp.java", 3, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
//	}
}
