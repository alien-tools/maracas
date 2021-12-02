package com.github.maracas.visitors;

import java.util.Set;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class InterfaceAddedVisitor extends SupertypeAddedVisitor {

	public InterfaceAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> intersRef) {
		super(clsRef, intersRef, JApiCompatibilityChange.INTERFACE_ADDED);
	}
}
