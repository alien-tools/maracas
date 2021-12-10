package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.EXTENDS;
import static com.github.maracas.brokenUse.APIUse.IMPLEMENTS;
import static com.github.maracas.brokenUse.APIUse.IMPORT;
import static com.github.maracas.brokenUse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.CLASS_REMOVED;

import org.junit.jupiter.api.Test;

class ClassRemovedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_REMOVED, 9);
	}

	@Test
	void testExtImport() {
		assertBrokenUse("ClassRemovedExt.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testExtExtends() {
		assertBrokenUse("ClassRemovedExt.java", 5, CLASS_REMOVED, EXTENDS);
	}

	@Test
	void testExtExtendsTypeDep() {
		assertBrokenUse("ClassRemovedExt.java", 5, CLASS_REMOVED, TYPE_DEPENDENCY);
	}

	@Test
	void testImpImport() {
		assertBrokenUse("ClassRemovedImp.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testImpImplements() {
		assertBrokenUse("ClassRemovedImp.java", 5, CLASS_REMOVED, IMPLEMENTS);
	}

	@Test
	void testTDImport() {
		assertBrokenUse("ClassRemovedTD.java", 3, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testTDImport2() {
		assertBrokenUse("ClassRemovedTD.java", 4, CLASS_REMOVED, IMPORT);
	}

	@Test
	void testTDTypeDep1() {
		assertBrokenUse("ClassRemovedTD.java", 8, CLASS_REMOVED, TYPE_DEPENDENCY);
	}

	@Test
	void testTDTypeDep2() {
		assertBrokenUse("ClassRemovedTD.java", 9, CLASS_REMOVED, TYPE_DEPENDENCY);
	}
}
