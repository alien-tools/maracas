package com.github.maracas.visitors;

import com.github.maracas.brokenuse.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Broken uses of METHOD_REMOVED are:
 *	- Any reference to the now-removed method (invocation, override)
 */
public class MethodRemovedVisitor extends BreakingChangeVisitor {
	private final CtExecutableReference<?> mRef;

	public MethodRemovedVisitor(CtExecutableReference<?> mRef) {
		super(JApiCompatibilityChange.METHOD_REMOVED);
		this.mRef = mRef;
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (mRef.equals(invocation.getExecutable())) {
			brokenUse(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (mRef.getExecutableDeclaration() instanceof CtMethod<?> method) {
			if (m.isOverriding(method))
				brokenUse(m, method, mRef, APIUse.METHOD_OVERRIDE);
		} else throw new RuntimeException("That should be a method though");
	}
}
