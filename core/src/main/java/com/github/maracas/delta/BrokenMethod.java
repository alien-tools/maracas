package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ConstructorRemovedVisitor;
import com.github.maracas.visitors.MethodNowAbstractVisitor;
import com.github.maracas.visitors.MethodNowFinalVisitor;
import com.github.maracas.visitors.MethodRemovedVisitor;

import japicmp.model.JApiBehavior;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;

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
				case METHOD_REMOVED      -> new MethodRemovedVisitor(mRef);
				case METHOD_NOW_FINAL    -> new MethodNowFinalVisitor(mRef);
				case METHOD_NOW_ABSTRACT -> new MethodNowAbstractVisitor(mRef);
				case CONSTRUCTOR_REMOVED -> new ConstructorRemovedVisitor(mRef);
				default -> null;
			};
	}
}
