package com.github.maracas.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtTypeReference;

public class SuperclassRemovedVisitor extends SupertypeRemovedVisitor {

	public SuperclassRemovedVisitor(CtTypeReference<?> clsRef, CtTypeReference<?> superRef) {
		super(clsRef, new HashSet<CtTypeReference<?>>(Arrays.asList(superRef)), JApiCompatibilityChange.SUPERCLASS_REMOVED);
	}
}
