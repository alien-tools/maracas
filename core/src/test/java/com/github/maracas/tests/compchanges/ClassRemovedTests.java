package com.github.maracas.tests.compchanges;

import static com.github.maracas.delta.APIUse.EXTENDS;
import static com.github.maracas.delta.APIUse.IMPLEMENTS;
import static com.github.maracas.delta.APIUse.IMPORT;
import static com.github.maracas.delta.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.CLASS_REMOVED;

import org.junit.jupiter.api.Test;

class ClassRemovedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_REMOVED, 8);
	}

	@Test
	void testExtImport() {
		assertDetection("ClassRemovedExt.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testExtExtends() {
		assertDetection("ClassRemovedExt.java", 5, CLASS_REMOVED, EXTENDS);
	}

	@Test
	void testImpImport() {
		assertDetection("ClassRemovedImp.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testImpImplements() {
		assertDetection("ClassRemovedImp.java", 5, CLASS_REMOVED, IMPLEMENTS);
	}

//	@Test
//	void testImpMethod() {
//		assertDetection("ClassRemovedImp.java", 8, CLASS_REMOVED, METHOD_OVERRIDE);
//	}

	@Test
	void testTDImport() {
		assertDetection("ClassRemovedTD.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testTDTypeDep1() {
		assertDetection("ClassRemovedTD.java", 8, CLASS_REMOVED, TYPE_DEPENDENCY);
	}

	@Test
	void testTDTypeDep2() {
		assertDetection("ClassRemovedTD.java", 9, CLASS_REMOVED, TYPE_DEPENDENCY);
	}
}