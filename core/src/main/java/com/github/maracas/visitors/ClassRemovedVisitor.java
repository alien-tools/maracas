package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering class removed issues in client code.
 */
public class ClassRemovedVisitor extends TypeReferenceVisitor {
	/**
	 * Creates a ClassRemovedVisitor instance.
	 *
	 * @param clsRef the now-removed class
	 */
	public ClassRemovedVisitor(CtTypeReference<?> clsRef) {
		super(clsRef, JApiCompatibilityChange.CLASS_REMOVED);
	}
}
