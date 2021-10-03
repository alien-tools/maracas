package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of FIELD_NO_LONGER_STATIC are:
 *	- Attempting to access a no-longer-static field in a static way
 */
public class FieldNoLongerStaticVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;

	public FieldNoLongerStaticVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_NO_LONGER_STATIC);
		this.fRef = fRef;
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
		// Need to handle the case where the static field of a parent class
		// is accessed through its subclass: SubClass.staticFieldInSuperClass
		CtTypeReference<?> accessedType = fieldAccess.getVariable().getDeclaringType();
		CtTypeReference<?> refType = fRef.getDeclaringType();

		if (
			accessedType.isSubtypeOf(refType) &&
			// Not a big fan of the simpleName comparison, but it should be safe and
			// the line below refuses to match
			fieldAccess.getVariable().getSimpleName().equals(fRef.getFieldDeclaration().getSimpleName()) &&
			//Objects.equal(fieldAccess.getVariable().getFieldDeclaration(), fRef.getFieldDeclaration())
			isStaticAccess(fieldAccess)
		)
			detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
	}

	private <T> boolean isStaticAccess(CtFieldAccess<T> fieldAccess) {
		// Target is a CtTypeAccess for static fields
		// and a CtExpression for everything else
		if (fieldAccess.getTarget() instanceof CtTypeAccess<?> ta) {
			// In a subclass, direct (unprefixed) access to a parent static field would
			// still be counted as a TypeAccess. We check if it's actually accessed
			// in an (explicit) static way with isImplicit()
			return !ta.isImplicit();
		}

		return false;
	}
}
