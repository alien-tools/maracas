package com.github.maracas.compchangestests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.compchangestests.CompChangesTest.assertBrokenUse;
import static com.github.maracas.compchangestests.CompChangesTest.assertNumberBrokenUses;
import static japicmp.model.JApiCompatibilityChange.FIELD_TYPE_CHANGED;

class FieldTypeChangedTests {
	@Disabled("Check false-positives later")
	@Test
	void testNoMore() {
		assertNumberBrokenUses(FIELD_TYPE_CHANGED, 68);
	}

	@Test
	void testAssignSameType1() {
		assertBrokenUse("FieldTypeChangedFA.java", 20, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType2() {
		assertBrokenUse("FieldTypeChangedFA.java", 21, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType3() {
		assertBrokenUse("FieldTypeChangedFA.java", 23, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType4() {
		assertBrokenUse("FieldTypeChangedFA.java", 24, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType5() {
		assertBrokenUse("FieldTypeChangedFA.java", 31, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testAssignSameType6() {
		assertBrokenUse("FieldTypeChangedFA.java", 33, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testAssignSameType7() {
		assertBrokenUse("FieldTypeChangedFA.java", 34, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testAssignSameType8() {
		assertBrokenUse("FieldTypeChangedFA.java", 35, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType9() {
		assertBrokenUse("FieldTypeChangedFA.java", 38, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType10() {
		assertBrokenUse("FieldTypeChangedFA.java", 40, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType11() {
		assertBrokenUse("FieldTypeChangedFA.java", 41, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType12() {
		assertBrokenUse("FieldTypeChangedFA.java", 43, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType13() {
		assertBrokenUse("FieldTypeChangedFA.java", 44, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType14() {
		assertBrokenUse("FieldTypeChangedFA.java", 47, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType15() {
		assertBrokenUse("FieldTypeChangedFA.java", 48, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCast1() {
		assertBrokenUse("FieldTypeChangedFA.java", 55, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCast2() {
		assertBrokenUse("FieldTypeChangedFA.java", 60, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType1() {
		assertBrokenUse("FieldTypeChangedFA.java", 71, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType2() {
		assertBrokenUse("FieldTypeChangedFA.java", 82, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testAssignCompatibleType3() {
		assertBrokenUse("FieldTypeChangedFA.java", 84, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testAssignCompatibleType4() {
		assertBrokenUse("FieldTypeChangedFA.java", 86, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType5() {
		assertBrokenUse("FieldTypeChangedFA.java", 89, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType6() {
		assertBrokenUse("FieldTypeChangedFA.java", 91, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType7() {
		assertBrokenUse("FieldTypeChangedFA.java", 92, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType8() {
		assertBrokenUse("FieldTypeChangedFA.java", 94, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType9() {
		assertBrokenUse("FieldTypeChangedFA.java", 95, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType1() {
		assertBrokenUse("FieldTypeChangedFA.java", 108, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType2() {
		assertBrokenUse("FieldTypeChangedFA.java", 109, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType3() {
		assertBrokenUse("FieldTypeChangedFA.java", 115, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType4() {
		assertBrokenUse("FieldTypeChangedFA.java", 119, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteSameType5() {
		assertBrokenUse("FieldTypeChangedFA.java", 121, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteSameType6() {
		assertBrokenUse("FieldTypeChangedFA.java", 122, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteSameType7() {
		assertBrokenUse("FieldTypeChangedFA.java", 123, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType8() {
		assertBrokenUse("FieldTypeChangedFA.java", 128, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType9() {
		assertBrokenUse("FieldTypeChangedFA.java", 129, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType10() {
		assertBrokenUse("FieldTypeChangedFA.java", 131, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType11() {
		assertBrokenUse("FieldTypeChangedFA.java", 132, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType12() {
		assertBrokenUse("FieldTypeChangedFA.java", 134, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType13() {
		assertBrokenUse("FieldTypeChangedFA.java", 136, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType1() {
		assertBrokenUse("FieldTypeChangedFA.java", 145, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType2() {
		assertBrokenUse("FieldTypeChangedFA.java", 146, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType3() {
		assertBrokenUse("FieldTypeChangedFA.java", 156, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteCompatibleType4() {
		assertBrokenUse("FieldTypeChangedFA.java", 158, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteCompatibleType5() {
		assertBrokenUse("FieldTypeChangedFA.java", 159, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Disabled("japicmp doesn't support generics")
	@Test
	void testWriteCompatibleType6() {
		assertBrokenUse("FieldTypeChangedFA.java", 160, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType7() {
		assertBrokenUse("FieldTypeChangedFA.java", 165, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType8() {
		assertBrokenUse("FieldTypeChangedFA.java", 166, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType9() {
		assertBrokenUse("FieldTypeChangedFA.java", 168, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType10() {
		assertBrokenUse("FieldTypeChangedFA.java", 169, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType11() {
		assertBrokenUse("FieldTypeChangedFA.java", 171, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType12() {
		assertBrokenUse("FieldTypeChangedFA.java", 173, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuper1() {
		assertBrokenUse("FieldTypeChangedFA.java", 182, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuper2() {
		assertBrokenUse("FieldTypeChangedFA.java", 183, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testTernary() {
		assertBrokenUse("FieldTypeChangedFA.java", 192, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testLambda1() {
		assertBrokenUse("FieldTypeChangedFA.java", 194, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testLambda2() {
		assertBrokenUse("FieldTypeChangedFA.java", 197, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInvocation1() {
		assertBrokenUse("FieldTypeChangedFA.java", 202, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInvocation2() {
		assertBrokenUse("FieldTypeChangedFA.java", 203, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testOperatorAssignment() {
		assertBrokenUse("FieldTypeChangedFA.java", 206, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testBinaryOperator() {
		assertBrokenUse("FieldTypeChangedFA.java", 208, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testArraySize() {
		assertBrokenUse("FieldTypeChangedFA.java", 209, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testArryElement() {
		assertBrokenUse("FieldTypeChangedFA.java", 210, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInstanceOf() {
		assertBrokenUse("FieldTypeChangedFA.java", 212, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWhile1() {
		assertBrokenUse("FieldTypeChangedFA.java", 214, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testDo() {
		assertBrokenUse("FieldTypeChangedFA.java", 215, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testIf() {
		assertBrokenUse("FieldTypeChangedFA.java", 216, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testElse() {
		assertBrokenUse("FieldTypeChangedFA.java", 217, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWhile2() {
		assertBrokenUse("FieldTypeChangedFA.java", 218, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testFor() {
		assertBrokenUse("FieldTypeChangedFA.java", 219, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThrow() {
		assertBrokenUse("FieldTypeChangedFA.java", 222, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testField1() {
		assertBrokenUse("FieldTypeChangedFA.java", 228, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testField2() {
		assertBrokenUse("FieldTypeChangedFA.java", 229, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testFieldAccess() {
		assertBrokenUse("FieldTypeChangedFA.java", 235, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testExt1() {
		assertBrokenUse("FieldTypeChangedExt.java", 16, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThis1() {
		assertBrokenUse("FieldTypeChangedExt.java", 17, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testSuper1() {
		assertBrokenUse("FieldTypeChangedExt.java", 18, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testExt2() {
		assertBrokenUse("FieldTypeChangedExt.java", 19, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThis2() {
		assertBrokenUse("FieldTypeChangedExt.java", 20, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testSuper2() {
		assertBrokenUse("FieldTypeChangedExt.java", 21, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}
}
