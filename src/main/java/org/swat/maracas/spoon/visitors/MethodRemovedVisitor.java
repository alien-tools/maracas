package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.delta.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Detections of METHOD_REMOVED are:
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
			detection(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (mRef.getExecutableDeclaration() instanceof CtMethod<?> method) {
			if (m.isOverriding(method))
				detection(m, method, mRef, APIUse.METHOD_OVERRIDE);
		} else throw new RuntimeException("That should be a method though");
	}
}
