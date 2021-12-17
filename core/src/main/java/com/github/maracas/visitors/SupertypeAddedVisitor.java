package com.github.maracas.visitors;

import com.github.maracas.brokenUse.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

	// FIXME: Is there a way to avoid running this on every non-abstract class in the client?
	// e.g. by starting with some stricter conditions on the classes we want to look at
	@Override
	public <T> void visitCtClass(CtClass<T> cls) {
		if (!cls.isAbstract()) {
			CtTypeReference<?> typeRef = cls.getReference();
			Set<CtTypeReference<?>> interfaces = typeRef.getSuperInterfaces().stream()
				.filter(ref -> ref.getTypeDeclaration() != null)
				.collect(Collectors.toSet());

			if (SpoonTypeHelpers.isSubtype(interfaces, clsRef))
				brokenUse(cls, cls, clsRef, APIUse.IMPLEMENTS);

			if (typeRef.getSuperclass() != null)
				if (SpoonTypeHelpers.isSubtype(Set.of(typeRef.getSuperclass()), clsRef))
					brokenUse(cls, cls, clsRef, APIUse.EXTENDS);
		}
	}
}
