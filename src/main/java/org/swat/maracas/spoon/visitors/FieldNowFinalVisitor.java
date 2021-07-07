package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.reference.CtFieldReference;

/**
 * Detections of FIELD_NOW_FINAL are:
 *	- Attempting to write-access a now-final field
 */
public class FieldNowFinalVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;

	public FieldNowFinalVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_NOW_FINAL);
		this.fRef = fRef;
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		if (fRef.equals(fieldWrite.getVariable()))
			detection(fieldWrite, fieldWrite.getVariable(), fRef, APIUse.FIELD_ACCESS);
	}
}
