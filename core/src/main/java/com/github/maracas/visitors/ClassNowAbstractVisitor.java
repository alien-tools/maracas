package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_NOW_ABSTRACT are:
 *	- Instantiations of the now-abstract class
 */
public class ClassNowAbstractVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;

	public ClassNowAbstractVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_NOW_ABSTRACT);
		this.clsRef = clsRef;
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
		if (clsRef.equals(ctConstructorCall.getType()))
				detection(ctConstructorCall, ctConstructorCall.getType(), clsRef, APIUse.INSTANTIATION);
	}
}
