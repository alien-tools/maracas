package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.METHOD_RETURN_TYPE_CHANGED;

import org.junit.jupiter.api.Test;

public class MethodReturnTypeChangedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_RETURN_TYPE_CHANGED, 4);
	}

	@Test
	void testInvokeWidenedMeth() {
		assertDetection("MethodReturnTypeChangedMI.java", 16, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeWidenedMethSuper() {
		assertDetection("MethodReturnTypeChangedExt.java", 14, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeWidenedMethNoSuper() {
		assertDetection("MethodReturnTypeChangedExt.java", 22, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideNarrowedMeth() {
		assertDetection("MethodReturnTypeChangedImp.java", 15, METHOD_RETURN_TYPE_CHANGED, METHOD_OVERRIDE);
	}
}
