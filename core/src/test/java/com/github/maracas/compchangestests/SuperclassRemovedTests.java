package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_REMOVED;

import org.junit.jupiter.api.Test;

public class SuperclassRemovedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberDetections(SUPERCLASS_REMOVED, 8);
		// FIXME: Check other cases
	}
	
	@Test
	void testExtendsAbsClass() {
		assertDetection("SuperclassRemovedExtAbs.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}
	
	@Test
	void testExtendsMultiAbsClass() {
		assertDetection("SuperclassRemovedImp.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}
	
	@Test
	void testExtendsMultiMultiAbsClass() {
		assertDetection("SuperclassRemovedImpMulti.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}
	
	@Test
	void testExtendsTransAbsClass() {
		assertDetection("SuperSuperclassRemovedExt.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}
}
