package com.github.maracas.visitors;

import java.util.Optional;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Generic type reference visitor. It creates a detection for every reference
 * to the supplied {code clsRef}:
 *   - Every type reference to it
 *   - Every reference to a field that it declares
 *   - Every reference to an executable that it declares
 *   - Every method that overrides one of its methods
 *
 * FIXME: do we really want that, or should we be dumber?
 */
public class TypeReferenceVisitor extends BreakingChangeVisitor {
	protected final CtTypeReference<?> clsRef;

	public TypeReferenceVisitor(JApiCompatibilityChange change, CtTypeReference<?> clsRef) {
		super(change);
		this.clsRef = clsRef;
	}

	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		if (clsRef.equals(reference)) {
			APIUse use = getAPIUseByRole(reference);

			detection(reference.getParent(), reference, clsRef, use);
		}
	}

	@Override
	public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
		if (clsRef.equals(reference.getDeclaringType()))
			detection(reference.getParent(), reference.getFieldDeclaration(), clsRef, APIUse.FIELD_ACCESS);
	}

	@Override
	public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
		if (clsRef.equals(reference.getDeclaringType()))
			detection(reference.getParent(), reference.getExecutableDeclaration(), clsRef, APIUse.METHOD_INVOCATION);
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (m.hasAnnotation(java.lang.Override.class)) {
			Optional<CtMethod<?>> superMethod =
				m.getTopDefinitions()
					.stream()
					.filter(superM -> clsRef.equals(superM.getDeclaringType().getReference()))
					.findAny();

			if (superMethod.isPresent())
				detection(m, superMethod.get(), clsRef, APIUse.METHOD_OVERRIDE);
		}
	}
}
