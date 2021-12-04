package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;

/**
 * Visitor in charge of gathering all constructor
 * removed issues in client code.
 * <p>
 * CONSTRUCTOR_REMOVED detected cases:
 * <ul>
 * <li> Any call to the removed constructor.
 *      Example: <pre>new TypeName();</pre>
 * <li> Call to the removed constructor when creating
 *      an anonymous class.
 *      Example: <pre>new AnonymClass() { }</pre>
 * <li> Invocation to super() constructor.
 *      Example: <pre>super()</pre>
 * </ul>
 */
public class ConstructorRemovedVisitor extends BreakingChangeVisitor {

	private final CtExecutableReference<?> mRef;

	public ConstructorRemovedVisitor(CtExecutableReference<?> mRef) {
		super(JApiCompatibilityChange.CONSTRUCTOR_REMOVED);
		this.mRef = mRef;
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> consCall) {
		if (mRef.equals(consCall.getExecutable())) {
			detection(consCall, consCall.getExecutable(), mRef, APIUse.INSTANTIATION);
		}
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> anonymClass) {
		CtTypeReference<?> superclass = anonymClass.getAnonymousClass().getSuperclass();

		if (superclass != null)
			superclass.getDeclaredExecutables()
				.stream()
				.filter(exec -> mRef.equals(exec))
				.forEach(exec ->
					detection(anonymClass, exec, mRef, APIUse.INSTANTIATION)
				);
	}
	
	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		// Every invocation of a constructor refers to super()
		if (mRef.equals(invocation.getExecutable())) {
			detection(invocation, invocation.getExecutable(), mRef, APIUse.METHOD_INVOCATION);
		}
		// FIXME: some cases do not throw a compilation error: 
		// super() refers to the Object type constructor 
	}
}
