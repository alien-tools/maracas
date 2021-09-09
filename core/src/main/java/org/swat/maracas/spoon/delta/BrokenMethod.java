package org.swat.maracas.spoon.delta;

import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;
import org.swat.maracas.spoon.visitors.MethodNowAbstractVisitor;
import org.swat.maracas.spoon.visitors.MethodNowFinalVisitor;
import org.swat.maracas.spoon.visitors.MethodRemovedVisitor;

import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;

public class BrokenMethod extends AbstractBrokenDeclaration {
	private final JApiMethod jApiMethod;
	private final CtExecutableReference<?> mRef;

	public BrokenMethod(JApiMethod method, CtExecutableReference<?> mRef, JApiCompatibilityChange change) {
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
				default -> null;
			};
	}
}
