package com.github.maracas.visitors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.units.qual.C;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.CtExecutableReferenceImpl;

public class InterfaceAddedVisitor extends BreakingChangeVisitor {

	protected final CtTypeReference<?> clsRef;
	protected final Set<CtTypeReference<?>> intersRef;
	
	public InterfaceAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> intersRef) {
		super(JApiCompatibilityChange.INTERFACE_ADDED);
		this.clsRef = clsRef;
		this.intersRef = intersRef;
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
