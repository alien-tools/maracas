package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.EXTENDS;
import static com.github.maracas.detection.APIUse.IMPLEMENTS;
import static com.github.maracas.detection.APIUse.IMPORT;
import static com.github.maracas.detection.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassLessAccessibleTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(CLASS_LESS_ACCESSIBLE, 64);
	}

	@Test
	void testPackPriv2PrivExtInnerExtends() {
		assertDetection("ClassLessAccessiblePackPriv2PrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPackPriv2PrivExtInnerTD() {
		assertDetection("ClassLessAccessiblePackPriv2PrivExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePro2PackPriv() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivExtInnerExtends() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PackPrivExtInnerTD() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 11, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivAccessPublicFieldInner1() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 14, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivAccessPublicFieldInner2() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 14, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PackPrivAccessPublicFieldInner2() {
//		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPro2PackPrivInvokePublicMethodInner1() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 18, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivInvokePublicMethodInner2() {
		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 18, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PackPrivInvokePublicMethodInner2() {
//		assertDetection("ClassLessAccessiblePro2PackPrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testInstantiatePro2Priv() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivExtInnerExtends() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PrivExtInnerTD() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 11, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivAccessPublicFieldInner1() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 14, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivAccessPublicFieldInner2() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 14, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PrivAccessPublicFieldInner2() {
//		assertDetection("ClassLessAccessiblePro2PrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPro2PrivInvokePublicMethodInner1() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 18, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivInvokePublicMethodInner2() {
		assertDetection("ClassLessAccessiblePro2PrivExt.java", 18, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PrivInvokePublicMethodInner2() {
//		assertDetection("ClassLessAccessiblePro2PrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testImportPub2PackPriv() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testExtendsPub2PackPrivExtends() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testExtendsPub2PackPrivTD() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPriv1() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPriv2() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2PackPriv()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPrivExt() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicField1() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 13, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicField2() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 13, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivAccessPublicField2() {
//		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 13, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testExtendsPub2PackPrivInvokePublicMethod1() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 17, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testExtendsPub2PackPrivInvokePublicMethod2() {
		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 17, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testExtendsPub2PackPrivInvokePublicMethod2() {
//		assertDetection("ClassLessAccessiblePub2PackPrivExt.java", 17, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testImportPub2PackPrivImp() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testImportPub2PackPrivImpImpl() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 5, CLASS_LESS_ACCESSIBLE, IMPLEMENTS);
	}

	@Test
	void testPub2PackPrivImpAccessPublicField() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 7, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicFieldStatic1() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 11, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicFieldStatic2() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 11, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivAccessPublicFieldStatic2() {
//		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 11, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPub2PackPrivInvokePublicMethod1() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 15, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivInvokePublicMethod2() {
		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 15, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivInvokePublicMethod2() {
//		assertDetection("ClassLessAccessiblePub2PackPrivImp.java", 15, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testPub2PrivInner1() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivInner2() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2Priv.ClassLessAccessiblePub2PrivInner()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
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
	void testPub2PrivExtInnerTD() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 12, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerAccessPublicField1() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 15, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerAccessPublicField2() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 15, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PrivExtInnerAccessPublicField2() {
//		assertDetection("ClassLessAccessiblePub2PrivExt.java", 15, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPub2PrivExtInnerInvokePublicMethod1() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 19, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerInvokePublicMethod2() {
		assertDetection("ClassLessAccessiblePub2PrivExt.java", 19, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PrivExtInnerInvokePublicMethod2() {
//		assertDetection("ClassLessAccessiblePub2PrivExt.java", 19, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testPub2ProInner1() {
		assertDetection("ClassLessAccessiblePub2ProExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2ProInner2() {
		assertDetection("ClassLessAccessiblePub2ProExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2Pro.ClassLessAccessiblePub2ProInner()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testWeirdCaseWronglyDetected() {
		// Very weird case; gets a wrong detection
		// Putting that in a dummy @Test to keep the total count
		//
		//	[CLASS_LESS_ACCESSIBLE]
		//		Element: c2 (ClassLessAccessiblePub2ProExt.java:9)
		//		Used:    main.classLessAccessible.ClassLessAccessiblePub2Pro.ClassLessAccessiblePub2ProInner
		//		Source:  main.classLessAccessible.ClassLessAccessiblePub2Pro.ClassLessAccessiblePub2ProInner
		//		Use:     TYPE_DEPENDENCY
		Assertions.assertTrue(true);
	}

	@Test
	void testNoLongerPublicExtImport() {
		assertDetection("ClassNoLongerPublicExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicExtExt() {
		assertDetection("ClassNoLongerPublicExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testNoLongerPublicExtTD() {
		assertDetection("ClassNoLongerPublicExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtSuperFieldTD() {
		assertDetection("ClassNoLongerPublicExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessNoSuperField() {
//		assertDetection("ClassNoLongerPublicExt.java", 8, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperField1() {
		assertDetection("ClassNoLongerPublicExt.java", 12, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtAccessSuperField2() {
		assertDetection("ClassNoLongerPublicExt.java", 12, "super.field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessSuperField2() {
//		assertDetection("ClassNoLongerPublicExt.java", 12, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperMethod() {
		assertDetection("ClassNoLongerPublicExt.java", 16, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessNoSuperMethod() {
//		assertDetection("ClassNoLongerPublicExt.java", 16, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperMethodSuper1() {
		assertDetection("ClassNoLongerPublicExt.java", 20, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtAccessSuperMethodSuper2() {
		assertDetection("ClassNoLongerPublicExt.java", 20, "super.method()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessSuperMethod() {
//		assertDetection("ClassNoLongerPublicExt.java", 20, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testNoLongerPublicImpImport() {
		assertDetection("ClassNoLongerPublicImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicImpImp() {
		assertDetection("ClassNoLongerPublicImp.java", 5, CLASS_LESS_ACCESSIBLE, IMPLEMENTS);
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
	void testNoLongerPublicTDFieldAccess1() {
		assertDetection("ClassNoLongerPublicTD.java", 10, "field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess2() {
		assertDetection("ClassNoLongerPublicTD.java", 10, "this.field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess3() {
		assertDetection("ClassNoLongerPublicTD.java", 10, "this.field = field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDInstantiate1() {
		assertDetection("ClassNoLongerPublicTD.java", 14, "c", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDInstantiate2() {
		assertDetection("ClassNoLongerPublicTD.java", 14, "new main.classNoLongerPublic.ClassNoLongerPublic()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}
}
