package com.github.maracas.visitors;

import java.util.Optional;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.util.TypeCompatibilityHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all method return
 * type changed issues in client code.
 * <p>
 * METHOD_RETURN_TYPE_CHANGED detected cases:
 * <ul>
 * <li> Invocation of the method in an assignment.
 * <li> Invocation of the method in a return statement.
 * <li> Invocation of the method in a type cast.
 * <li> Client method overriding the library's method.
 * </ul>
 */
public class MethodReturnTypeChangedVisitor extends BreakingChangeVisitor {

	private final CtExecutableReference<?> mRef;
	private final CtTypeReference<?> newType;

	public MethodReturnTypeChangedVisitor(CtExecutableReference<?> mRef, CtTypeReference<?> newType) {
		super(JApiCompatibilityChange.METHOD_RETURN_TYPE_CHANGED);
		this.mRef = mRef;
		this.newType = newType;
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (mRef.equals(invocation.getExecutable())) {
			CtElement parent = invocation.getParent();
			Optional<CtTypeReference<?>> typeRefOpt;

			// The enclosing statement should be an assignment or a return
			// statement.
			// FIXME: are there issues with type casts?
			if (parent instanceof CtAssignment<?, ?> enclosing) {
				typeRefOpt = Optional.of(enclosing.getType());
			} else if (parent instanceof CtReturn<?> enclosing) {
				typeRefOpt = Optional.of(invocation.getExecutable().getType());
			} else {
				typeRefOpt = Optional.empty();
			}

			if (typeRefOpt.isPresent() && !TypeCompatibilityHelpers.isAssignableFrom(typeRefOpt.get(), newType)) {
				detection(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
			}
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (mRef.getExecutableDeclaration() instanceof CtMethod<?> method) {
			CtTypeReference<?> expectedType = SpoonHelpers.inferExpectedType(method);

			if (m.isOverriding(method) && !TypeCompatibilityHelpers.isAssignableFrom(newType, expectedType)) {
				detection(m, method, mRef, APIUse.METHOD_OVERRIDE);
			}
		} else {
			throw new RuntimeException(String.format("%s should be a method.", mRef.getSimpleName()));
		}
	}
}
