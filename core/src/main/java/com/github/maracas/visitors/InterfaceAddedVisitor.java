package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * Visitor in charge of gathering all interface added issues in client code.
 */
public class InterfaceAddedVisitor extends SupertypeAddedVisitor {
	public InterfaceAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> newInterfaces) {
		super(clsRef, newInterfaces, JApiCompatibilityChange.INTERFACE_ADDED);
	}
}
