package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.INSTANTIATION;
import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static japicmp.model.JApiCompatibilityChange.CONSTRUCTOR_REMOVED;

import org.junit.jupiter.api.Test;

public class ConstructorRemovedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(CONSTRUCTOR_REMOVED, 12); // Diff coming from ClassNowAbstract
	}

	@Test
	void invokeNoParams() {
		assertDetection("ConstructorRemovedMI.java", 9, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeParams() {
		assertDetection("ConstructorRemovedMI.java", 13, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeNoParamsAnonymous() {
		assertDetection("ConstructorRemovedMI.java", 17, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeParamsAnonymous() {
		assertDetection("ConstructorRemovedMI.java", 21, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeSuperNoParams() {
		assertDetection("ConstructorRemovedExtNoParams.java", 8, CONSTRUCTOR_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void invokeNoParamsParent() {
		assertDetection("ConstructorRemovedExtNoParams.java", 12, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}

	@Test
	void invokeSuperParams() {
		assertDetection("ConstructorRemovedExtParams.java", 8, CONSTRUCTOR_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void invokeParamsParent() {
		assertDetection("ConstructorRemovedExtParams.java", 12, CONSTRUCTOR_REMOVED, INSTANTIATION);
	}
}
