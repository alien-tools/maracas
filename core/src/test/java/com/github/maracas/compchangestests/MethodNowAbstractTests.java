package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.EXTENDS;
import static com.github.maracas.detection.APIUse.IMPLEMENTS;
import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static japicmp.model.JApiCompatibilityChange.METHOD_NOW_ABSTRACT;

import org.junit.jupiter.api.Test;

class MethodNowAbstractTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(METHOD_NOW_ABSTRACT, 14);
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

	@Test
	void testStaticToAbstractImplementation() {
		assertDetection("MethodNoLongerStaticImp.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testStaticToAbstractInvocation() {
		assertDetection("MethodNoLongerStaticImp.java", 8, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
}
