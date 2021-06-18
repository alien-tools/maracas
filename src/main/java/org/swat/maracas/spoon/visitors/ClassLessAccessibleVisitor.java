package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.Detection.APIUse;

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

	protected ClassLessAccessibleVisitor(CtTypeReference<?> clsRef, AccessModifier newAccessModifier) {
		super(JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE);
		this.clsRef = clsRef;
		this.newAccessModifier = newAccessModifier;
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

			// If private, always breaks
			if (newAccessModifier == AccessModifier.PRIVATE)
				detection(reference.getParent(), reference, clsRef, use);

			// If protected (inner), breaks if not a subtype
			if (newAccessModifier == AccessModifier.PROTECTED)
				if (!reference.getParent(CtType.class).isSubtypeOf(clsRef))
					detection(reference.getParent(), reference, clsRef, use);

			// If package private, breaks if != package
			if (newAccessModifier == AccessModifier.PACKAGE_PROTECTED) {
				// FIXME: ...
				String refFqn = reference.getQualifiedName();
				String clsRefFqn = clsRef.getQualifiedName();
				String refPkg = refFqn.substring(0, refFqn.lastIndexOf("."));
				String clsRefPkg = clsRefFqn.substring(0, clsRefFqn.lastIndexOf("."));
				if (!refPkg.equals(clsRefPkg))
					detection(reference.getParent(), reference, clsRef, use);
			}
		}

		super.visitCtTypeReference(reference);
	}

	@Override
	public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
		if (clsRef.equals(reference.getDeclaringType()))
			detection(reference.getParent(), reference.getFieldDeclaration(), clsRef, APIUse.FIELD_ACCESS);

		super.visitCtFieldReference(reference);
	}

	@Override
	public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
		if (clsRef.equals(reference.getDeclaringType()))
			detection(reference.getParent(), reference.getExecutableDeclaration(), clsRef, APIUse.METHOD_INVOCATION);

		super.visitCtExecutableReference(reference);
	}
}
