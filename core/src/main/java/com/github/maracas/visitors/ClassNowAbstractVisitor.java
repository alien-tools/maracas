package com.github.maracas.visitors;

import com.github.maracas.brokenUse.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.reference.CtTypeReference;

/**
 * Broken uses of CLASS_NOW_ABSTRACT are:
 *	- Instantiations of the now-abstract class
 *
 * Note: In JApiCmp, types that go from {@code class} to {@code interface} are impacted
 * by CLASS_NOW_ABSTRACT the same way types that go from {@code class} to
 * {@code abstract class} are.
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
				brokenUse(ctConstructorCall, ctConstructorCall.getType(), clsRef, APIUse.INSTANTIATION);
	}
}
