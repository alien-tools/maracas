package com.github.maracas.visitors;

import java.util.Set;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all interface added issues in client code.
 */
public class InterfaceAddedVisitor extends SupertypeAddedVisitor {

	public InterfaceAddedVisitor(CtTypeReference<?> clsRef, 
			Set<CtTypeReference<?>> intersRef) {
		super(clsRef, intersRef, JApiCompatibilityChange.INTERFACE_ADDED);
	}
}
