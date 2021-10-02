package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonHelpers;

import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtType;
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

	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		if (clsRef.equals(reference)) {
			APIUse use = getAPIUseByRole(reference);

			String enclosingPkg = SpoonHelpers.getEnclosingPkgName(reference);
			String expectedPkg = SpoonHelpers.getEnclosingPkgName(clsRef.getTypeDeclaration());

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
