package com.github.maracas.visitors;

import com.github.maracas.delta.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.path.CtRole;
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
			CtRole role = reference.getRoleInParent();
			APIUse use = switch (role) {
				case CAST, DECLARING_TYPE, TYPE, ARGUMENT_TYPE, ACCESSED_TYPE, TYPE_ARGUMENT, THROWN, MULTI_TYPE ->
					APIUse.TYPE_DEPENDENCY;
				case SUPER_TYPE ->
					APIUse.EXTENDS;
				case INTERFACE ->
					APIUse.IMPLEMENTS;
				case ANNOTATION_TYPE ->
					APIUse.ANNOTATION;
				default ->
					throw new RuntimeException("Unmanaged role " + role);
			};

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
