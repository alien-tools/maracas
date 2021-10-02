package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

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
}
