package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.EXTENDS;
import static com.github.maracas.detection.APIUse.IMPLEMENTS;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_ADDED;

import org.junit.jupiter.api.Test;

public class InterfaceAddedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(INTERFACE_ADDED, 8);
	}
	
	@Test
	void testExtendsAbsClass() {
		assertDetection("InterfaceAddedExtAbs.java", 5, INTERFACE_ADDED, EXTENDS);
	}
	
	@Test
	void testImplementsInter() {
		assertDetection("InterfaceAddedImp.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}
	
	@Test
	void testImplementsMultiInter() {
		assertDetection("InterfaceAddedImpMulti.java", 5, INTERFACE_ADDED, IMPLEMENTS);
	}
}
