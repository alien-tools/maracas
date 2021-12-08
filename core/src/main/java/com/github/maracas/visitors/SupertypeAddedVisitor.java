package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.Set;

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
	protected final Set<CtTypeReference<?>> newTypes;

	protected SupertypeAddedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> newTypes, JApiCompatibilityChange change) {
		super(change);
		this.clsRef = clsRef;
		this.newTypes = newTypes;
	}

	@Override
	public <T> void visitCtClass(CtClass<T> cls) {
		if (!cls.isAbstract()) {
			CtTypeReference<?> typeRef = cls.getReference();
			Set<CtTypeReference<?>> interfaces = new HashSet<>(typeRef.getSuperInterfaces());

			if (SpoonTypeHelpers.isSubtype(interfaces, clsRef))
				detection(cls, cls, clsRef, APIUse.IMPLEMENTS);

			if (typeRef.getSuperclass() != null)
				if (SpoonTypeHelpers.isSubtype(Set.of(typeRef.getSuperclass()), clsRef))
					detection(cls, cls, clsRef, APIUse.EXTENDS);
		}
	}
}
