package com.github.maracas.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.util.SpoonTypeHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all method added to interface issues in 
 * client code.
 * 
 * <p>METHOD_ADDED_TO_INTERFACE detected cases:
 * <ul>
 * <li> Concrete classes directly implementing the interface.
 * <li> Concrete classes transitively implementing the interface.
 * </ul>
 */
public class MethodAddedToInterfaceVisitor extends BreakingChangeVisitor {

	private final CtTypeReference<?> clsRef;

	public MethodAddedToInterfaceVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.METHOD_ADDED_TO_INTERFACE);
		this.clsRef = clsRef;
	}

	@Override
	public <T> void visitCtClass(CtClass<T> cls) {
		if (!cls.isAbstract()) {
			CtTypeReference<?> typeRef = cls.getReference();
			Set<CtTypeReference<?>> interfaces = new HashSet<>(typeRef.getSuperInterfaces());
			Set<CtTypeReference<?>> superCls = new HashSet<>(Arrays.asList(typeRef.getSuperclass()));

			if (SpoonTypeHelpers.isSubtype(interfaces, clsRef)) {
				detection(cls, cls, clsRef, APIUse.IMPLEMENTS);
			}
			
			if (SpoonTypeHelpers.isSubtype(superCls, clsRef)) {
				detection(cls, cls, clsRef, APIUse.EXTENDS);
			}
		}
	}
}

