package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ConstructorRemovedVisitor;
import com.github.maracas.visitors.MethodNowAbstractVisitor;
import com.github.maracas.visitors.MethodNowFinalVisitor;
import com.github.maracas.visitors.MethodRemovedVisitor;
import com.github.maracas.visitors.MethodReturnTypeChangedVisitor;
import japicmp.model.JApiBehavior;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiMethod;
import javassist.NotFoundException;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Represents a method-level breaking change
 */
public class MethodBreakingChange extends AbstractBreakingChange {
	private final JApiBehavior jApiMethod;
	private final CtExecutableReference<?> mRef;

	public MethodBreakingChange(JApiBehavior method, CtExecutableReference<?> mRef, JApiCompatibilityChange change) {
		super(change);
		this.jApiMethod = method;
		this.mRef = mRef;
	}

	@Override
	public CtReference getReference() {
		return mRef;
	}

	@Override
	public BreakingChangeVisitor getVisitor() {
		return
			switch (change) {
				case CONSTRUCTOR_REMOVED        -> new ConstructorRemovedVisitor(mRef);
				case METHOD_REMOVED             -> new MethodRemovedVisitor(mRef);
				case METHOD_NOW_FINAL           -> new MethodNowFinalVisitor(mRef);
				case METHOD_NOW_ABSTRACT        -> new MethodNowAbstractVisitor(mRef);
				case METHOD_RETURN_TYPE_CHANGED -> {
					try {
						// Thanks for the checked exception japi <3
						String newTypeName = ((JApiMethod) jApiMethod).getNewMethod().get().getReturnType().getName();
						CtTypeReference<?> newType = mRef.getFactory().Type().createReference(newTypeName);
						yield new MethodReturnTypeChangedVisitor(mRef, newType);
					} catch (NotFoundException e) {
						throw new IllegalStateException("japicmp gave us a METHOD_RETURN_TYPE_CHANGED without the new type of the method");
					}
				}
				case METHOD_LESS_ACCESSIBLE -> null; // TODO: To be implemented
				case METHOD_IS_STATIC_AND_OVERRIDES_NOT_STATIC -> null; // TODO: To be implemented
				case METHOD_NOW_STATIC -> null; // TODO: To be implemented
				case METHOD_NO_LONGER_STATIC -> null; // TODO: To be implemented
				case METHOD_ADDED_TO_INTERFACE -> null; // TODO: To be implemented
				case METHOD_NOW_THROWS_CHECKED_EXCEPTION -> null; // TODO: To be implemented
				case METHOD_NO_LONGER_THROWS_CHECKED_EXCEPTION -> null; // TODO: To be implemented
				case METHOD_ABSTRACT_NOW_DEFAULT -> null; // TODO: To be implemented
				case CONSTRUCTOR_LESS_ACCESSIBLE -> null; // TODO: To be implemented
				case ANNOTATION_DEPRECATED_ADDED -> null; // TODO: To be implemented
				default -> throw new IllegalStateException(this + " was somehow associated to a non-method-level breaking change: " + change);
			};
	}
}
