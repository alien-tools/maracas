package com.github.maracas.tests.compchanges;

import static com.github.maracas.delta.APIUse.FIELD_ACCESS;
import static japicmp.model.JApiCompatibilityChange.FIELD_TYPE_CHANGED;

import org.junit.jupiter.api.Test;

class FieldTypeChangedTests extends CompChangesTest {
	@Test
	void testNoMore() {
		assertNumberDetections(FIELD_TYPE_CHANGED, 79);
	}

	@Test
	void testAssignSameType1() {
		assertDetection("FieldTypeChangedFA.java", 20, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType2() {
		assertDetection("FieldTypeChangedFA.java", 21, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType3() {
		assertDetection("FieldTypeChangedFA.java", 23, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType4() {
		assertDetection("FieldTypeChangedFA.java", 24, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType5() {
		assertDetection("FieldTypeChangedFA.java", 31, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType6() {
		assertDetection("FieldTypeChangedFA.java", 33, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType7() {
		assertDetection("FieldTypeChangedFA.java", 34, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType8() {
		assertDetection("FieldTypeChangedFA.java", 35, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType9() {
		assertDetection("FieldTypeChangedFA.java", 38, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType10() {
		assertDetection("FieldTypeChangedFA.java", 40, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType11() {
		assertDetection("FieldTypeChangedFA.java", 41, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType12() {
		assertDetection("FieldTypeChangedFA.java", 43, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType13() {
		assertDetection("FieldTypeChangedFA.java", 44, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType14() {
		assertDetection("FieldTypeChangedFA.java", 47, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignSameType15() {
		assertDetection("FieldTypeChangedFA.java", 48, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCast1() {
		assertDetection("FieldTypeChangedFA.java", 55, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCast2() {
		assertDetection("FieldTypeChangedFA.java", 60, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType1() {
		assertDetection("FieldTypeChangedFA.java", 71, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType2() {
		assertDetection("FieldTypeChangedFA.java", 82, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType3() {
		assertDetection("FieldTypeChangedFA.java", 84, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType4() {
		assertDetection("FieldTypeChangedFA.java", 86, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType5() {
		assertDetection("FieldTypeChangedFA.java", 89, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType6() {
		assertDetection("FieldTypeChangedFA.java", 91, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType7() {
		assertDetection("FieldTypeChangedFA.java", 92, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType8() {
		assertDetection("FieldTypeChangedFA.java", 94, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAssignCompatibleType9() {
		assertDetection("FieldTypeChangedFA.java", 95, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType1() {
		assertDetection("FieldTypeChangedFA.java", 108, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType2() {
		assertDetection("FieldTypeChangedFA.java", 109, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType3() {
		assertDetection("FieldTypeChangedFA.java", 115, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType4() {
		assertDetection("FieldTypeChangedFA.java", 119, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType5() {
		assertDetection("FieldTypeChangedFA.java", 121, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType6() {
		assertDetection("FieldTypeChangedFA.java", 122, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType7() {
		assertDetection("FieldTypeChangedFA.java", 123, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType8() {
		assertDetection("FieldTypeChangedFA.java", 128, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType9() {
		assertDetection("FieldTypeChangedFA.java", 129, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType10() {
		assertDetection("FieldTypeChangedFA.java", 131, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType11() {
		assertDetection("FieldTypeChangedFA.java", 132, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType12() {
		assertDetection("FieldTypeChangedFA.java", 134, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteSameType13() {
		assertDetection("FieldTypeChangedFA.java", 136, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType1() {
		assertDetection("FieldTypeChangedFA.java", 145, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType2() {
		assertDetection("FieldTypeChangedFA.java", 146, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType3() {
		assertDetection("FieldTypeChangedFA.java", 156, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType4() {
		assertDetection("FieldTypeChangedFA.java", 158, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType5() {
		assertDetection("FieldTypeChangedFA.java", 159, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType6() {
		assertDetection("FieldTypeChangedFA.java", 160, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType7() {
		assertDetection("FieldTypeChangedFA.java", 165, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType8() {
		assertDetection("FieldTypeChangedFA.java", 166, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType9() {
		assertDetection("FieldTypeChangedFA.java", 168, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType10() {
		assertDetection("FieldTypeChangedFA.java", 169, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType11() {
		assertDetection("FieldTypeChangedFA.java", 171, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWriteCompatibleType12() {
		assertDetection("FieldTypeChangedFA.java", 173, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuper1() {
		assertDetection("FieldTypeChangedFA.java", 182, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testAccessSuper2() {
		assertDetection("FieldTypeChangedFA.java", 183, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testTernary() {
		assertDetection("FieldTypeChangedFA.java", 192, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testLambda1() {
		assertDetection("FieldTypeChangedFA.java", 194, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testLambda2() {
		assertDetection("FieldTypeChangedFA.java", 197, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInvocation1() {
		assertDetection("FieldTypeChangedFA.java", 202, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInvocation2() {
		assertDetection("FieldTypeChangedFA.java", 203, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testOperatorAssignment() {
		assertDetection("FieldTypeChangedFA.java", 206, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testBinaryOperator() {
		assertDetection("FieldTypeChangedFA.java", 208, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testArraySize() {
		assertDetection("FieldTypeChangedFA.java", 209, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testArryElement() {
		assertDetection("FieldTypeChangedFA.java", 210, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testInstanceOf() {
		assertDetection("FieldTypeChangedFA.java", 212, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWhile1() {
		assertDetection("FieldTypeChangedFA.java", 214, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testDo() {
		assertDetection("FieldTypeChangedFA.java", 215, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testIf() {
		assertDetection("FieldTypeChangedFA.java", 216, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testElse() {
		assertDetection("FieldTypeChangedFA.java", 217, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testWhile2() {
		assertDetection("FieldTypeChangedFA.java", 218, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testFor() {
		assertDetection("FieldTypeChangedFA.java", 219, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThrow() {
		assertDetection("FieldTypeChangedFA.java", 222, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testField1() {
		assertDetection("FieldTypeChangedFA.java", 228, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testField2() {
		assertDetection("FieldTypeChangedFA.java", 229, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testFieldAccess() {
		assertDetection("FieldTypeChangedFA.java", 235, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testExt1() {
		assertDetection("FieldTypeChangedExt.java", 16, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThis1() {
		assertDetection("FieldTypeChangedExt.java", 17, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testSuper1() {
		assertDetection("FieldTypeChangedExt.java", 18, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testExt2() {
		assertDetection("FieldTypeChangedExt.java", 19, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testThis2() {
		assertDetection("FieldTypeChangedExt.java", 20, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}

	@Test
	void testSuper2() {
		assertDetection("FieldTypeChangedExt.java", 21, FIELD_TYPE_CHANGED, FIELD_ACCESS);
	}
}
