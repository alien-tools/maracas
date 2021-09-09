package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_REMOVED are:
 *   - Any reference to the now-removed class
 */
public class ClassRemovedVisitor extends TypeReferenceVisitor {
	public ClassRemovedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_REMOVED, clsRef);
	}
}
