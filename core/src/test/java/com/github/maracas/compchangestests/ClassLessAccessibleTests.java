package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.*;
import static japicmp.model.JApiCompatibilityChange.*;

import org.junit.jupiter.api.Test;

class ClassLessAccessibleTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_LESS_ACCESSIBLE, 2);
	}

	@Test
	void testPackPriv2PrivExtInner() {
		assertDetection("ClassLessAccessiblePackPriv2PrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testInstantiatePro2PackPriv() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivExtInner() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PackPrivAccessPublicFieldInner() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 14, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPro2PackPrivInvokePublicMethodInner() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 18, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testInstantiatePro2Priv() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivExtInner() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PrivAccessPublicFieldInner() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 14, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPro2PrivInvokePublicMethodInner() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 18, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testImportPub2PackPriv() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testExtendsPub2PackPriv() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testInstantiatePub2PackPriv() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPrivExt() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicField() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 13, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 13, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testExtendsPub2PackPrivInvokePublicMethod() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 17, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 17, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testImportPub2PackPrivImp() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testPub2PackPrivImpAccessPublicField() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 7, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2PackPrivAccessPublicFieldStatic() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 11, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 11, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2PackPrivInvokePublicMethod() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 15, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 15, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testPub2PrivInner() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInner() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerExt() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 12, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPub2PrivExtInnerAccessPublicField() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 15, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 15, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testPub2PrivExtInnerInvokePublicMethod() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 19, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 19, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testPub2ProInner() {
		assertDetection("ClassLessAccessiblePub2ProExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	// Testing the old CLASS_NO_LONGER_PUBLIC tests
	@Test
	void testNoLongerPublicExtImport() {
		assertDetection("ClassNoLongerPublicExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicExtExt() {
		assertDetection("ClassNoLongerPublicExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testNoLongerPublicExtAccessNoSuperField() {
		assertDetection("ClassNoLongerPublicExt.java", 8, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testNoLongerPublicExtAccessSuperField() {
		assertDetection("ClassNoLongerPublicExt.java", 12, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
		assertDetection("ClassNoLongerPublicExt.java", 12, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testNoLongerPublicExtAccessNoSuperMethod() {
		assertDetection("ClassNoLongerPublicExt.java", 16, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testNoLongerPublicExtAccessSuperMethod() {
		assertDetection("ClassNoLongerPublicExt.java", 20, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
	}

	@Test
	void testNoLongerPublicImpImport() {
		assertDetection("ClassNoLongerPublicImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicImpImp() {
		assertDetection("ClassNoLongerPublicImp.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testNoLongerPublicTDImport() {
		assertDetection("ClassNoLongerPublicTD.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicTDType() {
		assertDetection("ClassNoLongerPublicTD.java", 7, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDParam() {
		assertDetection("ClassNoLongerPublicTD.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess() {
		assertDetection("ClassNoLongerPublicTD.java", 10, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
	}

	@Test
	void testNoLongerPublicTDInstantiate() {
		assertDetection("ClassNoLongerPublicTD.java", 14, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}
}
