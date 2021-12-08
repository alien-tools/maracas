package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.IMPLEMENTS;
import static japicmp.model.JApiCompatibilityChange.METHOD_ADDED_TO_INTERFACE;

import org.junit.jupiter.api.Test;

public class MethodAddedToInterfaceTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_ADDED_TO_INTERFACE, 4);
	}
	
	@Test
	void testClassImpl1() {
		assertDetection("MethodAddedToInterfaceImp1.java", 5, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}
	
	@Test
	void testClassImpl2() {
		assertDetection("MethodAddedToInterfaceImp2.java", 5, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}
	
	@Test
	void testInnerClassImpl1() {
		assertDetection("MethodAddedToInterfaceInnerImp.java", 7, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}
	
	@Test
	void testInnerClassImpl2() {
		assertDetection("MethodAddedToInterfaceInnerExt.java", 7, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
	}
	
//	@Test
//	void testClassImplTrans() {
//		assertDetection("MethodAddedToInterfaceTransImp.java", 3, METHOD_ADDED_TO_INTERFACE, IMPLEMENTS);
//	}
}
