package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * Visitor in charge of gathering all superclass added issues in client code.
 */
public class SuperclassAddedVisitor extends SupertypeAddedVisitor {
	public SuperclassAddedVisitor(CtTypeReference<?> clsRef, CtTypeReference<?> newClass) {
		super(clsRef, Set.of(newClass), JApiCompatibilityChange.SUPERCLASS_ADDED);
	}
}
