package com.github.maracas.visitors;

import com.github.maracas.delta.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.reference.CtFieldReference;

/**
 * Detections of FIELD_REMOVED are:
 *	- Any reference to the now-removed field
 */
public class FieldRemovedVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;

	public FieldRemovedVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_REMOVED);
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
		if (fRef.equals(fieldAccess.getVariable()))
			detection(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
	}
}
