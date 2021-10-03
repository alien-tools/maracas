package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * FIXME: the current implementation only targets @Deprecated types
 */
public class AnnotationDeprecatedAddedVisitor extends TypeReferenceVisitor {
	public AnnotationDeprecatedAddedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED, clsRef);
	}
}
