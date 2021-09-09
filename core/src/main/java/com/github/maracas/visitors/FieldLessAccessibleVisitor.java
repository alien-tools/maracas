package com.github.maracas.visitors;

import com.github.maracas.delta.APIUse;

import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;

/**
 * Detections of FIELD_LESS_ACCESSIBLE are:
 *	- Any access to a now-private field
 *	- Any access to a now-package-private field outside the package
 *	- Any access to a now-protected field outside its subtype hierarchy or package
 *
 *	TODO: what about inner classes (e.g. using private fields of the outer?)
 */
public class FieldLessAccessibleVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;
	private final AccessModifier newAccessModifier;

	public FieldLessAccessibleVisitor(CtFieldReference<?> fRef, AccessModifier newAccessModifier) {
		super(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE);
		this.fRef = fRef;
		this.newAccessModifier = newAccessModifier;
	}

	@Override
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		visitCtFieldAccess(fieldRead);
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		visitCtFieldAccess(fieldWrite);
	}

	private <T> void visitCtFieldAccess(CtFieldAccess<T> fieldAccess) {
		if (fRef.equals(fieldAccess.getVariable())) {
			String enclosingPkg = getEnclosingPkgName(fieldAccess);
			String expectedPkg = getEnclosingPkgName(fRef.getFieldDeclaration());

			switch (newAccessModifier) {
				// Private always breaks
				case PRIVATE:
					detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
					break;
				// Package-private breaks if packages do not match
				case PACKAGE_PROTECTED:
					if (!enclosingPkg.equals(expectedPkg))
						detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
					break;
				// Protected fails if not a subtype and packages do not match
				case PROTECTED:
					if (!fieldAccess.getParent(CtType.class).isSubtypeOf(fRef.getDeclaringType()) &&
						!enclosingPkg.equals(expectedPkg))
						detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
					break;
				default:
					// Can't happen
			}
		}
	}
}
