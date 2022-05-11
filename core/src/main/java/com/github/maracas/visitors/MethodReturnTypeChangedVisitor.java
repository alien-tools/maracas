package com.github.maracas.visitors;

import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all method return type changed issues in
 * client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Invocations to the method in a statement where the expected type is not
 *      compatible with the new type.
 *      <pre>
 *      ArrayList a = methodReturnsListNow();
 *      </pre>
 * <li> Methods overriding the modified method where the new type is not compatible
 *      with the client method type.
 *      <pre>
 *      &#64;Override
 *      public long methodNowReturnsInt() { return 1; }
 *      </pre>
 * </ul>
 */
public class MethodReturnTypeChangedVisitor extends BreakingChangeVisitor {
	/**
	 * Spoon reference to the modified method.
	 */
	private final CtExecutableReference<?> mRef;

	/**
	 * Spoon reference to the new return type of the modified method.
	 */
	private final CtTypeReference<?> newType;

	/**
	 * Actual method that has been changed
	 */
	private final CtMethod<?> method;

	/**
	 * Type expected by the new method
	 */
	private final CtTypeReference<?> expectedType;

	/**
	 * Creates a MethodReturnTypeChangedVisitor instance.
	 *
	 * @param mRef    the modified method
	 * @param newType the new return type of the modified method
	 */
	public MethodReturnTypeChangedVisitor(CtExecutableReference<?> mRef, CtTypeReference<?> newType) {
		super(JApiCompatibilityChange.METHOD_RETURN_TYPE_CHANGED);
		this.mRef = mRef;
		this.newType = newType;
		this.method = (CtMethod<?>) mRef.getExecutableDeclaration();
		this.expectedType = SpoonTypeHelpers.inferExpectedType(method);
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (mRef.equals(invocation.getExecutable())) {
			CtElement parent = invocation.getParent();
			CtTypeReference<?> expectedType = SpoonTypeHelpers.inferExpectedType(parent);
			// FIXME: are there issues with type casts?
			if (expectedType != null && !SpoonTypeHelpers.isAssignableFrom(expectedType, newType))
				brokenUse(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (m.getSignature().equals(method.getSignature()) && m.isOverriding(method)
			&& !SpoonTypeHelpers.isAssignableFromNoBoxing(newType, expectedType))
			brokenUse(m, method, mRef, APIUse.METHOD_OVERRIDE);
	}
}
