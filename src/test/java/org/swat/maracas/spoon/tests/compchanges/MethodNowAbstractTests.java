package org.swat.maracas.spoon.tests.compchanges;

import static japicmp.model.JApiCompatibilityChange.*;
import static org.swat.maracas.spoon.Detection.APIUse.*;

import org.junit.jupiter.api.Test;

class MethodNowAbstractTests extends CompChangesTest {
	// TODO: when a method goes from static (with body) to non-static (abstract)
	//       in an interface, japicmp reports both a METHOD_NO_LONGER_STATIC and
	//       METHOD_NOW_ABSTRACT
	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_NOW_ABSTRACT, 12);
	}

	@Test
	void testExtNoImpl() {
		assertDetection("MethodNowAbstractExt.java", 5, METHOD_NOW_ABSTRACT, EXTENDS);
	}
	
	@Test
	void testExtInvocationSuper() {
		assertDetection("MethodNowAbstractExt.java", 13, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
	
	@Test
	void testExtInvocation() {
		assertDetection("MethodNowAbstractExt.java", 17, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
	
	@Test
	void testExtNoImpl2() {
		assertDetection("MethodNowAbstractExt2.java", 5, METHOD_NOW_ABSTRACT, EXTENDS);
	}
	
	@Test
	void testExtInvocationSuper2() {
		assertDetection("MethodNowAbstractExt2.java", 13, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
	
	@Test
	void testExtInvocation2() {
		assertDetection("MethodNowAbstractExt2.java", 17, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
	
	@Test
	void testImpNoImpl() {
		assertDetection("MethodNowAbstractImp.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}
	
	@Test
	void testImpNoImpl2() {
		assertDetection("MethodNowAbstractImp2.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}
	
	@Test
	void testMIinstantiateNowAbstractAnonymous() {
		assertDetection("MethodNowAbstractMI.java", 10, METHOD_NOW_ABSTRACT, EXTENDS);
	}
	
	@Test
	void testMIinstantiateNowAbstractSubAnonymous() {
		assertDetection("MethodNowAbstractMI.java", 34, METHOD_NOW_ABSTRACT, EXTENDS);
	}
	
	@Test
	void testMIinstantiateNowAbstractIntfAnonymous() {
		assertDetection("MethodNowAbstractMI.java", 58, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}
	
	@Test
	void testMIinstantiateNowAbstractIntfSubAnonymous() {
		assertDetection("MethodNowAbstractMI.java", 82, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}
}
