package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.METHOD_REMOVED;
import static org.swat.maracas.spoon.delta.APIUse.*;

import org.junit.jupiter.api.Test;

class MethodRemovedTests extends CompChangesTest {
	// We're just checking the cases related to METHOD_REMOVED directly,
	// not those caused by other removals
	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_REMOVED, 6);
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
}
