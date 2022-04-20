package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * FIXME: the current implementation only targets @Deprecated types
 */
public class AnnotationDeprecatedAddedToClassVisitor extends TypeReferenceVisitor {
	public AnnotationDeprecatedAddedToClassVisitor(CtTypeReference<?> clsRef) {
		super(clsRef, JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED);
	}
}
