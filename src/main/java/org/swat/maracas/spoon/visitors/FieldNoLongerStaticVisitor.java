package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtFieldReference;

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
		if (fRef.equals(fieldAccess.getVariable()) && isStaticAccess(fieldAccess))
			detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
	}

	private <T> boolean isStaticAccess(CtFieldAccess<T> fieldAccess) {
		if (fieldAccess.getTarget() instanceof CtTypeAccess<?> ta) {
			// In a subclass, direct (unprefixed) access to a parent static field would
			// still be counted as a TypeAccess. We check if it's actually accessed
			// in a static way with isImplicit()
			return !ta.isImplicit();
		}

		return false;
	}
}
