package org.swat.maracas.spoon.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class AnnotationDeprecatedAddedVisitor extends TypeReferenceVisitor {
	protected AnnotationDeprecatedAddedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED, clsRef);
	}
}
