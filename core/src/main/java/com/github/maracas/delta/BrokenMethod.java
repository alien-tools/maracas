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
 * A broken method is a special case of a broken declaration
 * that pinpoints methods in the library's code impacted
 * by a breaking change.
 */
public class BrokenMethod extends AbstractBrokenDeclaration {
	private final JApiBehavior jApiMethod;
	private final CtExecutableReference<?> mRef;

	public BrokenMethod(JApiBehavior method, CtExecutableReference<?> mRef, JApiCompatibilityChange change) {
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
						String newTypeName = ((JApiMethod) jApiMethod).getNewMethod().get().getReturnType().getName();
						CtTypeReference<?> newType = mRef.getFactory().Type().createReference(newTypeName);
						yield new MethodReturnTypeChangedVisitor(mRef, newType);
					} catch (NotFoundException e) {
						yield null;
					}
				}
				default -> null; // FIXME: should eventually disappear
			};
	}
}
