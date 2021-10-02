package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.INSTANTIATION;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_ABSTRACT;

import org.junit.jupiter.api.Test;

class ClassNowAbstractTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_NOW_ABSTRACT, 4);
	}

	@Test
	void testInstantiateAbstractClass() {
		assertDetection("ClassNowAbstractMI.java", 8, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateAbstractClassParams() {
		assertDetection("ClassNowAbstractMI.java", 12, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateInterface() {
		assertDetection("IClassNowAbstractMI.java", 8, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testInstantiateInterfaceParams() {
		assertDetection("IClassNowAbstractMI.java", 12, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}
}
