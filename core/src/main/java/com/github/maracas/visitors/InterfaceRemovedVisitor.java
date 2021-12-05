package com.github.maracas.visitors;

import java.util.Set;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class InterfaceRemovedVisitor extends SupertypeRemovedVisitor {

	public InterfaceRemovedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> supersRef) {
		super(clsRef, supersRef, JApiCompatibilityChange.INTERFACE_REMOVED);
	}

}
