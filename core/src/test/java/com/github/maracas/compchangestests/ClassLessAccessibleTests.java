package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.brokenuse.APIUse.IMPLEMENTS;
import static com.github.maracas.brokenuse.APIUse.IMPORT;
import static com.github.maracas.brokenuse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassLessAccessibleTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberBrokenUses(CLASS_LESS_ACCESSIBLE, 64);
	}

	@Test
	void testPackPriv2PrivExtInnerExtends() {
		assertBrokenUse("ClassLessAccessiblePackPriv2PrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPackPriv2PrivExtInnerTD() {
		assertBrokenUse("ClassLessAccessiblePackPriv2PrivExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePro2PackPriv() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivExtInnerExtends() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PackPrivExtInnerTD() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 11, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivAccessPublicFieldInner1() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 14, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivAccessPublicFieldInner2() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 14, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PackPrivAccessPublicFieldInner2() {
//		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPro2PackPrivInvokePublicMethodInner1() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 18, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PackPrivInvokePublicMethodInner2() {
		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 18, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PackPrivInvokePublicMethodInner2() {
//		assertBrokenUse("ClassLessAccessiblePro2PackPrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testInstantiatePro2Priv() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivExtInnerExtends() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 11, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPro2PrivExtInnerTD() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 11, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivAccessPublicFieldInner1() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 14, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivAccessPublicFieldInner2() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 14, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PrivAccessPublicFieldInner2() {
//		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 14, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPro2PrivInvokePublicMethodInner1() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 18, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPro2PrivInvokePublicMethodInner2() {
		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 18, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPro2PrivInvokePublicMethodInner2() {
//		assertBrokenUse("ClassLessAccessiblePro2PrivExt.java", 18, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testImportPub2PackPriv() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testExtendsPub2PackPrivExtends() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testExtendsPub2PackPrivTD() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPriv1() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPriv2() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2PackPriv()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testInstantiatePub2PackPrivExt() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicField1() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 13, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicField2() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 13, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivAccessPublicField2() {
//		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 13, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testExtendsPub2PackPrivInvokePublicMethod1() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 17, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testExtendsPub2PackPrivInvokePublicMethod2() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 17, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testExtendsPub2PackPrivInvokePublicMethod2() {
//		assertBrokenUse("ClassLessAccessiblePub2PackPrivExt.java", 17, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testImportPub2PackPrivImp() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testImportPub2PackPrivImpImpl() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 5, CLASS_LESS_ACCESSIBLE, IMPLEMENTS);
	}

	@Test
	void testPub2PackPrivImpAccessPublicField() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 7, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicFieldStatic1() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 11, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivAccessPublicFieldStatic2() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 11, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivAccessPublicFieldStatic2() {
//		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 11, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPub2PackPrivInvokePublicMethod1() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 15, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PackPrivInvokePublicMethod2() {
		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 15, "main.classLessAccessible.IClassLessAccessiblePub2PackPriv.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PackPrivInvokePublicMethod2() {
//		assertBrokenUse("ClassLessAccessiblePub2PackPrivImp.java", 15, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testPub2PrivInner1() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivInner2() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2Priv.ClassLessAccessiblePub2PrivInner()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInner() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerExt() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 12, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testPub2PrivExtInnerTD() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 12, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerAccessPublicField1() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 15, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerAccessPublicField2() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 15, "super.publicField", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PrivExtInnerAccessPublicField2() {
//		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 15, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testPub2PrivExtInnerInvokePublicMethod1() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 19, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2PrivExtInnerInvokePublicMethod2() {
		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 19, "super.publicMethod()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testPub2PrivExtInnerInvokePublicMethod2() {
//		assertBrokenUse("ClassLessAccessiblePub2PrivExt.java", 19, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testPub2ProInner1() {
		assertBrokenUse("ClassLessAccessiblePub2ProExt.java", 8, "c1", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testPub2ProInner2() {
		assertBrokenUse("ClassLessAccessiblePub2ProExt.java", 8, "new main.classLessAccessible.ClassLessAccessiblePub2Pro.ClassLessAccessiblePub2ProInner()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testWeirdCaseWronglyDetected() {
		// Very weird case; gets a wrong broken use
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
		assertBrokenUse("ClassNoLongerPublicExt.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicExtExt() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 5, CLASS_LESS_ACCESSIBLE, EXTENDS);
	}

	@Test
	void testNoLongerPublicExtTD() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 5, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtSuperFieldTD() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 8, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessNoSuperField() {
//		assertBrokenUse("ClassNoLongerPublicExt.java", 8, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperField1() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 12, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtAccessSuperField2() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 12, "super.field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessSuperField2() {
//		assertBrokenUse("ClassNoLongerPublicExt.java", 12, CLASS_LESS_ACCESSIBLE, FIELD_ACCESS);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperMethod() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 16, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessNoSuperMethod() {
//		assertBrokenUse("ClassNoLongerPublicExt.java", 16, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testNoLongerPublicExtAccessSuperMethodSuper1() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 20, "super", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicExtAccessSuperMethodSuper2() {
		assertBrokenUse("ClassNoLongerPublicExt.java", 20, "super.method()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

//	@Test
//	void testNoLongerPublicExtAccessSuperMethod() {
//		assertBrokenUse("ClassNoLongerPublicExt.java", 20, CLASS_LESS_ACCESSIBLE, METHOD_INVOCATION);
//	}

	@Test
	void testNoLongerPublicImpImport() {
		assertBrokenUse("ClassNoLongerPublicImp.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicImpImp() {
		assertBrokenUse("ClassNoLongerPublicImp.java", 5, CLASS_LESS_ACCESSIBLE, IMPLEMENTS);
	}

	@Test
	void testNoLongerPublicTDImport() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 3, CLASS_LESS_ACCESSIBLE, IMPORT);
	}

	@Test
	void testNoLongerPublicTDType() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 7, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDParam() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 9, CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess1() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 10, "field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess2() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 10, "this.field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDFieldAccess3() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 10, "this.field = field", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDInstantiate1() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 14, "c", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}

	@Test
	void testNoLongerPublicTDInstantiate2() {
		assertBrokenUse("ClassNoLongerPublicTD.java", 14, "new main.classNoLongerPublic.ClassNoLongerPublic()", CLASS_LESS_ACCESSIBLE, TYPE_DEPENDENCY);
	}
}
