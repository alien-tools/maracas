package org.swat.maracas.spoon.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class ClassLessAccessibleVisitor extends TypeReferenceVisitor {
	protected ClassLessAccessibleVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE, clsRef);
	}
}
