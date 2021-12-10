package com.github.maracas.visitors;

import java.util.Optional;

import com.github.maracas.brokenuse.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Broken uses of METHOD_NOW_FINAL are:
 *	- Methods overriding the now-final method (with or w/o explicit @Override)
 */
public class MethodNowFinalVisitor extends BreakingChangeVisitor {
	private final CtExecutableReference<?> mRef;

	public MethodNowFinalVisitor(CtExecutableReference<?> mRef) {
		super(JApiCompatibilityChange.METHOD_NOW_FINAL);
		this.mRef = mRef;
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		Optional<CtMethod<?>> superMethod =
			m.getTopDefinitions()
				.stream()
				.filter(superM -> mRef.equals(superM.getReference()))
				.findAny();

		superMethod.ifPresent(ctMethod -> brokenUse(m, ctMethod, mRef, APIUse.METHOD_OVERRIDE));
	}
}
