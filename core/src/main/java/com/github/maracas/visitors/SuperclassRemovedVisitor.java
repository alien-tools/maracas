package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * Visitor in charge of gathering all superclass removed issues in client code.
 */
public class SuperclassRemovedVisitor extends SupertypeRemovedVisitor {
	/**
	 * Creates a SuperclassRemovedVisitor instance.
	 *
	 * @param clsRef   reference to the client impacted class
	 * @param superRef removed superclass
	 */
	public SuperclassRemovedVisitor(CtTypeReference<?> clsRef, CtTypeReference<?> superRef) {
		super(clsRef, Set.of(superRef), JApiCompatibilityChange.SUPERCLASS_REMOVED);
	}
}
