package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.EXTENDS;
import static com.github.maracas.brokenUse.APIUse.IMPLEMENTS;
import static com.github.maracas.brokenUse.APIUse.METHOD_INVOCATION;
import static japicmp.model.JApiCompatibilityChange.METHOD_NOW_ABSTRACT;

import org.junit.jupiter.api.Test;

class MethodNowAbstractTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(METHOD_NOW_ABSTRACT, 14);
	}

	@Test
	void testExtNoImpl() {
		assertBrokenUse("MethodNowAbstractExt.java", 5, METHOD_NOW_ABSTRACT, EXTENDS);
	}

	@Test
	void testExtInvocationSuper() {
		assertBrokenUse("MethodNowAbstractExt.java", 13, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}

	@Test
	void testExtInvocation() {
		assertBrokenUse("MethodNowAbstractExt.java", 17, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}

	@Test
	void testExtNoImpl2() {
		assertBrokenUse("MethodNowAbstractExt2.java", 5, METHOD_NOW_ABSTRACT, EXTENDS);
	}

	@Test
	void testExtInvocationSuper2() {
		assertBrokenUse("MethodNowAbstractExt2.java", 13, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}

	@Test
	void testExtInvocation2() {
		assertBrokenUse("MethodNowAbstractExt2.java", 17, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}

	@Test
	void testImpNoImpl() {
		assertBrokenUse("MethodNowAbstractImp.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testImpNoImpl2() {
		assertBrokenUse("MethodNowAbstractImp2.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testMIinstantiateNowAbstractAnonymous() {
		assertBrokenUse("MethodNowAbstractMI.java", 10, METHOD_NOW_ABSTRACT, EXTENDS);
	}

	@Test
	void testMIinstantiateNowAbstractSubAnonymous() {
		assertBrokenUse("MethodNowAbstractMI.java", 34, METHOD_NOW_ABSTRACT, EXTENDS);
	}

	@Test
	void testMIinstantiateNowAbstractIntfAnonymous() {
		assertBrokenUse("MethodNowAbstractMI.java", 58, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testMIinstantiateNowAbstractIntfSubAnonymous() {
		assertBrokenUse("MethodNowAbstractMI.java", 82, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testStaticToAbstractImplementation() {
		assertBrokenUse("MethodNoLongerStaticImp.java", 5, METHOD_NOW_ABSTRACT, IMPLEMENTS);
	}

	@Test
	void testStaticToAbstractInvocation() {
		assertBrokenUse("MethodNoLongerStaticImp.java", 8, METHOD_NOW_ABSTRACT, METHOD_INVOCATION);
	}
}
