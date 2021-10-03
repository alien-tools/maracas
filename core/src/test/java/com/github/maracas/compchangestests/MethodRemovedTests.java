package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.METHOD_REMOVED;

import org.junit.jupiter.api.Test;

class MethodRemovedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_REMOVED, 19);
	}

	@Test
	void testInvokeRemoved() {
		assertDetection("MethodRemovedMI.java", 9, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedStatic() {
		assertDetection("MethodRemovedMI.java", 13, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeSuperRemoved() {
		assertDetection("MethodRemovedExt.java", 12, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemoved() {
		assertDetection("MethodRemovedExt.java", 16, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideInvokeSuperRemoved() {
		assertDetection("MethodRemovedExt.java", 17, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemovedImpl() {
		assertDetection("MethodRemovedImp.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	// METHOD_REMOVED detected because the containing classes are removed
	// or because it was removed from a super class
	@Test
	void testInvokeRemovedFromClass() {
		assertDetection("ClassRemovedImp.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuper1() {
		assertDetection("MethodRemovedInSuperclassExt.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuper2() {
		assertDetection("MethodRemovedInSuperclassExt.java", 13, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testInvokeRemovedInSuper1() {
		assertDetection("MethodRemovedInSuperclassExt.java", 18, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedInSuper2() {
		assertDetection("MethodRemovedInSuperclassExt.java", 22, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemovedInSuperExt1() {
		assertDetection("SMethodRemovedInSuperclassExt.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuperExt2() {
		assertDetection("SMethodRemovedInSuperclassExt.java", 13, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testInvokeRemovedInSuperExt1() {
		assertDetection("SMethodRemovedInSuperclassExt.java", 18, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedInSuperExt2() {
		assertDetection("SMethodRemovedInSuperclassExt.java", 22, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper1() {
		assertDetection("MethodRemovedInSuperclassFA.java", 10, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper2() {
		assertDetection("MethodRemovedInSuperclassFA.java", 15, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper3() {
		assertDetection("MethodRemovedInSuperclassFA.java", 20, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper4() {
		assertDetection("MethodRemovedInSuperclassFA.java", 25, METHOD_REMOVED, METHOD_INVOCATION);
	}
}
