package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of METHOD_NOW_ABSTRACT are:
 *	- Non-abstract types extending/implementing the enclosing type of the now-abstract method unless:
 *		- The now-abstract method is already implemented somewhere in the hierarchy
 *	- Invocations in subtypes of the now-abstract method
 */
public class MethodNowAbstractVisitor extends BreakingChangeVisitor {
	private final CtExecutableReference<?> mRef;

	public MethodNowAbstractVisitor(CtExecutableReference<?> mRef) {
		super(JApiCompatibilityChange.METHOD_NOW_ABSTRACT);
		this.mRef = mRef;
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		CtTypeReference<?> enclosingType = mRef.getDeclaringType();
		if (!ctClass.isAbstract() && ctClass.isSubtypeOf(enclosingType)) {
			if (mRef.getOverridingExecutable(ctClass.getReference()) == null) {
				if (enclosingType.isInterface())
					detection(ctClass, enclosingType, mRef, APIUse.IMPLEMENTS);
				else
					detection(ctClass, enclosingType, mRef, APIUse.EXTENDS);
			}
		}
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (mRef.equals(invocation.getExecutable())) {
			detection(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
		}
	}
}
