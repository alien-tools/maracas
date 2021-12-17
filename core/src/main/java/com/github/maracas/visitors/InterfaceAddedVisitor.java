package com.github.maracas.visitors;

import java.util.Set;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all interface added issues in client code.
 */
public class InterfaceAddedVisitor extends SupertypeAddedVisitor {
    /**
     * Creates a InterfaceAddedVisitor instance.
     *
     * @param clsRef        reference to the client impacted class
     * @param newInterfaces added interfaces
     */
	public InterfaceAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> newInterfaces) {
		super(clsRef, newInterfaces, JApiCompatibilityChange.INTERFACE_ADDED);
	}
}
