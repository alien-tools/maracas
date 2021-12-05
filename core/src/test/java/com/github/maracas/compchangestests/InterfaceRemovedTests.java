package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_REMOVED;

import org.junit.jupiter.api.Test;

public class InterfaceRemovedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(INTERFACE_REMOVED, 8);
		// FIXME: Check other cases
	}
	
//	@Test
//	void testExtendsAbsClass() {
//		assertDetection("InterfaceAddedExtAbs.java", 5, INTERFACE_REMOVED, METHOD_OVERRIDE);
//	}
}
