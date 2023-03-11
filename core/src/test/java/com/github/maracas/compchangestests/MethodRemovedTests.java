package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenuse.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.METHOD_REMOVED;

class MethodRemovedTests {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(METHOD_REMOVED, 19);
	}

	@Test
	void testInvokeRemoved() {
		assertBrokenUse("MethodRemovedMI.java", 9, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedStatic() {
		assertBrokenUse("MethodRemovedMI.java", 13, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeSuperRemoved() {
		assertBrokenUse("MethodRemovedExt.java", 12, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemoved() {
		assertBrokenUse("MethodRemovedExt.java", 16, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideInvokeSuperRemoved() {
		assertBrokenUse("MethodRemovedExt.java", 17, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemovedImpl() {
		assertBrokenUse("MethodRemovedImp.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	// METHOD_REMOVED detected because the containing classes are removed
	// or because it was removed from a super class
	@Test
	void testInvokeRemovedFromClass() {
		assertBrokenUse("ClassRemovedImp.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuper1() {
		assertBrokenUse("MethodRemovedInSuperclassExt.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuper2() {
		assertBrokenUse("MethodRemovedInSuperclassExt.java", 13, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testInvokeRemovedInSuper1() {
		assertBrokenUse("MethodRemovedInSuperclassExt.java", 18, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedInSuper2() {
		assertBrokenUse("MethodRemovedInSuperclassExt.java", 22, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideRemovedInSuperExt1() {
		assertBrokenUse("SMethodRemovedInSuperclassExt.java", 8, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideRemovedInSuperExt2() {
		assertBrokenUse("SMethodRemovedInSuperclassExt.java", 13, METHOD_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testInvokeRemovedInSuperExt1() {
		assertBrokenUse("SMethodRemovedInSuperclassExt.java", 18, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeRemovedInSuperExt2() {
		assertBrokenUse("SMethodRemovedInSuperclassExt.java", 22, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper1() {
		assertBrokenUse("MethodRemovedInSuperclassFA.java", 10, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper2() {
		assertBrokenUse("MethodRemovedInSuperclassFA.java", 15, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper3() {
		assertBrokenUse("MethodRemovedInSuperclassFA.java", 20, METHOD_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeMethodRemovedInSuper4() {
		assertBrokenUse("MethodRemovedInSuperclassFA.java", 25, METHOD_REMOVED, METHOD_INVOCATION);
	}
}
