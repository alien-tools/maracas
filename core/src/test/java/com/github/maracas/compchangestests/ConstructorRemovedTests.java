package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.INSTANTIATION;
import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.CONSTRUCTOR_REMOVED;

public class ConstructorRemovedTests {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(CONSTRUCTOR_REMOVED, 12); // Diff coming from ClassNowAbstract
	}

	@Test
	void invokeNoParams() {
		assertBrokenUse("ConstructorRemovedMI.java", 9, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeParams() {
		assertBrokenUse("ConstructorRemovedMI.java", 13, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeNoParamsAnonymous() {
		assertBrokenUse("ConstructorRemovedMI.java", 17, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeParamsAnonymous() {
		assertBrokenUse("ConstructorRemovedMI.java", 21, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeSuperNoParams() {
		assertBrokenUse("ConstructorRemovedExtNoParams.java", 8, CONSTRUCTOR_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void invokeNoParamsParent() {
		assertBrokenUse("ConstructorRemovedExtNoParams.java", 12, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeSuperParams() {
		assertBrokenUse("ConstructorRemovedExtParams.java", 8, CONSTRUCTOR_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void invokeParamsParent() {
		assertBrokenUse("ConstructorRemovedExtParams.java", 12, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}
}
