package com.github.maracas.visitors;

import java.util.Arrays;
import java.util.HashSet;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all superclass removed issues in client code.
 */
public class SuperclassRemovedVisitor extends SupertypeRemovedVisitor {

	public SuperclassRemovedVisitor(CtTypeReference<?> clsRef,
			CtTypeReference<?> superRef) {
		super(clsRef, new HashSet<CtTypeReference<?>>(Arrays.asList(superRef)),
				JApiCompatibilityChange.SUPERCLASS_REMOVED);
	}
}
