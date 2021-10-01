package com.github.maracas.compchangestests;

import static com.github.maracas.delta.APIUse.INSTANTIATION;
import static japicmp.model.JApiCompatibilityChange.CLASS_NOW_ABSTRACT;

import org.junit.jupiter.api.Test;

class ClassNowAbstractTests extends CompChangesTest {
	// TODO: japicmp reports CLASS_NOW_ABSTRACT for classes that go from Class to Interface
	//       Weird behavior => fix upstream?
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_NOW_ABSTRACT, 2);
	}

	@Test
	void testCreateObject() {
		assertDetection("ClassNowAbstractMI.java", 8, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}

	@Test
	void testExtMethod() {
		assertDetection("ClassNowAbstractMI.java", 12, CLASS_NOW_ABSTRACT, INSTANTIATION);
	}
}
