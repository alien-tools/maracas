package com.github.maracas.visitors;

import com.github.maracas.brokenUse.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Visitor in charge of gathering method removed issues in client code.
 *
 * The visitor detects the following cases:
 * <ul>
 * <li> Invocations to the now-removed method. Example:
 *      <pre>
 *      var a = method();
 *      </pre>
 * <li> Methods overriding the now-removed method. Example:
 *      <pre>
 *      &#64;Override
 *      public void method() { return; }
 *      </pre>
 * </ul>
 */
public class MethodRemovedVisitor extends BreakingChangeVisitor {
    /**
     * Spoon reference to the removed method.
     */
	private final CtExecutableReference<?> mRef;

	/**
	 * Creates a MethodRemovedVisitor instance.
	 * @param mRef the now-removed method
	 */
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
