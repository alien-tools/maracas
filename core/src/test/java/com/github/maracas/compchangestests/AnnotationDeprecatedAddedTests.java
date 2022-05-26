package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.EXTENDS;
import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.brokenuse.APIUse.IMPLEMENTS;
import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenuse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AnnotationDeprecatedAddedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(ANNOTATION_DEPRECATED_ADDED, 49);
	}

	@Test
	void testExtendDeprecatedClass() {
		assertBrokenUse("AnnotationDeprecatedAddedExt.java", 5, ANNOTATION_DEPRECATED_ADDED, EXTENDS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperFieldAccess() {
		assertBrokenUse("AnnotationDeprecatedAddedExt.java", 8, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperMethodInv() {
		assertBrokenUse("AnnotationDeprecatedAddedExt.java", 9, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperFieldAccessKeyword() {
		assertBrokenUse("AnnotationDeprecatedAddedExt.java", 13, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperMethodInvKeyword() {
		assertBrokenUse("AnnotationDeprecatedAddedExt.java", 14, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Test
	void testExtendDeprecatedClassTrans() {
		assertNoBrokenUse("AnnotationDeprecatedAddedExtSub.java", 5, ANNOTATION_DEPRECATED_ADDED, EXTENDS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperFieldAccessTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedExtSub.java", 8, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperMethodInvTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedExtSub.java", 9, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperFieldAccessKeywordTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedExtSub.java", 13, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testDeprecatedSuperMethodInvKeywordTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedExtSub.java", 14, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Test
	void testImplementsDeprecatedClass() {
		assertBrokenUse("AnnotationDeprecatedAddedImp.java", 5, ANNOTATION_DEPRECATED_ADDED, IMPLEMENTS);
	}

	@Test
	void testRefDeprecatedEmptyClassField() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 13, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testRefDeprecatedNonEmptyClassField() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 14, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Disabled("JApiCmp does not detect these cases")
	@Test
	void testRefDeprecatedClassTypeParamField() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 17, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testRefDeprecatedEmptyClassVar() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 20, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testRefDeprecatedNonEmptyClassVar() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 24, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Disabled("No implementation yet!")
	@Test
	void testFieldFromDeprecatedClass() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 25, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testMethodFromDeprecatedClass() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 26, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Test
	void testRefDeprecatedNonEmptyClassVarTrans() {
		assertNoBrokenUse("AnnotationDeprecatedAddedSA.java", 30, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Disabled("No implementation yet!")
	@Test
	void testFieldFromDeprecatedClassTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 31, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Disabled("No implementation yet!")
	@Test
	void testMethodFromDeprecatedClassTrans() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 32, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Test
	void testDeprecatedField() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 37, ANNOTATION_DEPRECATED_ADDED, FIELD_ACCESS);
	}

	@Test
	void testDeprecatedMethod() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 42, ANNOTATION_DEPRECATED_ADDED, METHOD_INVOCATION);
	}

	@Test
	void testDeprecatedAnonymEmptyClass() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 46, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testDeprecatedAnonymNonEmptyClass() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 47, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testDeprecatedAnonymInterface() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 51, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testDeprecatedClassAsParam() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 54, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Test
	void testDeprecatedClassAsReturnType() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 58, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}

	@Disabled("JApiCmp does not detect these cases")
	@Test
	void testDeprecatedClassAsTypeParam() {
		assertBrokenUse("AnnotationDeprecatedAddedSA.java", 63, ANNOTATION_DEPRECATED_ADDED, TYPE_DEPENDENCY);
	}
}
