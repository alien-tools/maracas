package com.github.maracas.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all supertype added issues in client code.
 * 
 * <p>INTERFACE_ADDED and SUPERCLASS_ADDED detected cases:
 * <ul>
 * <li> Classes extending the affected type.
 *      Example: <pre>public class C extends SuperC {}</pre>
 */
public class SupertypeAddedVisitor extends BreakingChangeVisitor {
	
	protected final CtTypeReference<?> clsRef;
	protected final Set<CtTypeReference<?>> supersRef;
	
	protected SupertypeAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> supersRef, JApiCompatibilityChange change) {
		super(change);
		this.clsRef = clsRef;
		this.supersRef = supersRef;
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
