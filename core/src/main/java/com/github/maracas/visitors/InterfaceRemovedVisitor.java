package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * Visitor in charge of gathering all interface removed issues in client code.
 */
public class InterfaceRemovedVisitor extends SupertypeRemovedVisitor {
	/**
	 * Creates a InterfaceRemovedVisitor instance.
	 *
	 * @param clsRef     reference to the client impacted class
	 * @param interfaces set of removed interfaces
	 */
	public InterfaceRemovedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> interfaces) {
		super(clsRef, interfaces, JApiCompatibilityChange.INTERFACE_REMOVED);
	}
}
