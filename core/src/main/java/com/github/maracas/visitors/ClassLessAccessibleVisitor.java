package com.github.maracas.visitors;

import com.github.maracas.delta.APIUse;

import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class ClassLessAccessibleVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;
	private final AccessModifier newAccessModifier;

	public ClassLessAccessibleVisitor(CtTypeReference<?> clsRef, AccessModifier newAccessModifier) {
		super(JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE);
		this.clsRef = clsRef;
		this.newAccessModifier = newAccessModifier;
	}

	// FIXME: import errors aren't supported
	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		if (clsRef.equals(reference)) {
			CtRole role = reference.getRoleInParent();
			APIUse use = switch (role) {
				// FIXME: try to distinguish between regular access to a type,
				// and access to a type by instantiation (new)
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

			String enclosingPkg = getEnclosingPkgName(reference);
			String expectedPkg = getEnclosingPkgName(clsRef.getTypeDeclaration());

			switch (newAccessModifier) {
				// Private always breaks
				case PRIVATE:
					detection(reference.getParent(), reference, clsRef, use);
					break;
				// Package-private breaks if packages do not match
				case PACKAGE_PROTECTED:
					if (!enclosingPkg.equals(expectedPkg))
						detection(reference.getParent(), reference, clsRef, use);
					break;
				// Protected fails if not a subtype and packages do not match
				case PROTECTED:
					if (!reference.getParent(CtType.class).isSubtypeOf(clsRef) &&
						!enclosingPkg.equals(expectedPkg))
						detection(reference.getParent(), reference, clsRef, use);
					break;
				default:
					// Can't happen
			}
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
