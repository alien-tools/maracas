package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenuse.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.METHOD_RETURN_TYPE_CHANGED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MethodReturnTypeChangedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(METHOD_RETURN_TYPE_CHANGED, 7);
	}

	@Test
	void testInvokeWidenedMeth() {
		assertBrokenUse("MethodReturnTypeChangedMI.java", 16, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Disabled("Not all casts generate a compilation error")
	@Test
    void testInvokeWidenedCast() {
        assertNoBrokenUse("MethodReturnTypeChangedMI.java", 26, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
    }

	@Test
	void testInvokeWidenedMethSuper() {
		assertBrokenUse("MethodReturnTypeChangedExt.java", 14, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Test
	void testInvokeWidenedMethNoSuper() {
		assertBrokenUse("MethodReturnTypeChangedExt.java", 22, METHOD_RETURN_TYPE_CHANGED, METHOD_INVOCATION);
	}

	@Test
	void testOverrideNarrowedMeth() {
		assertBrokenUse("MethodReturnTypeChangedImp.java", 15, METHOD_RETURN_TYPE_CHANGED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideWidenedMeth() {
		assertNoBrokenUse("MethodReturnTypeChangedImp.java", 10, METHOD_RETURN_TYPE_CHANGED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideBoxedMeth() {
		assertBrokenUse("MethodReturnTypeChangedImp.java", 20, METHOD_RETURN_TYPE_CHANGED, METHOD_OVERRIDE);
	}

	@Test
	void testOverrideUnboxedMeth() {
		assertBrokenUse("MethodReturnTypeChangedImp.java", 25, METHOD_RETURN_TYPE_CHANGED, METHOD_OVERRIDE);
	}
}
