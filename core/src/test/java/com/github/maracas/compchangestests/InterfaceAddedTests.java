package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.brokenuse.APIUse.IMPLEMENTS;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_ADDED;

import org.junit.jupiter.api.Test;

public class InterfaceAddedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(INTERFACE_ADDED, 8);
		// FIXME: Check other cases
	}
	
	@Test
	void testExtendsAbsClass() {
		assertBrokenUse("InterfaceAddedExtAbs.java", 5, INTERFACE_ADDED, EXTENDS);
	}
	
	@Test
	void testImplementsInter() {
		assertBrokenUse("InterfaceAddedImp.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}
	
	@Test
	void testImplementsMultiInter() {
		assertBrokenUse("InterfaceAddedImpMulti.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}
}
