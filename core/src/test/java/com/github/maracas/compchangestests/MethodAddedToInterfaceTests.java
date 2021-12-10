package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.IMPLEMENTS;
import static japicmp.model.JApiCompatibilityChange.METHOD_ADDED_TO_INTERFACE;

import org.junit.jupiter.api.Test;

public class MethodAddedToInterfaceTests extends CompChangesTest {

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
