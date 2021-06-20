package org.swat.maracas.spoon.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_REMOVED are:
 *   - Any reference to the now-removed class
 */
public class ClassRemovedVisitor extends TypeReferenceVisitor {
	protected ClassRemovedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_REMOVED, clsRef);
	}
}