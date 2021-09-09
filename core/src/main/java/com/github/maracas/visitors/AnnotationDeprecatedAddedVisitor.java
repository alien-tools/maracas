package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class AnnotationDeprecatedAddedVisitor extends TypeReferenceVisitor {
	public AnnotationDeprecatedAddedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED, clsRef);
	}
}
