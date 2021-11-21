package com.github.maracas.delta;

import com.github.maracas.visitors.AnnotationDeprecatedAddedVisitor;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ClassLessAccessibleVisitor;
import com.github.maracas.visitors.ClassNowAbstractVisitor;
import com.github.maracas.visitors.ClassNowCheckedExceptionVisitor;
import com.github.maracas.visitors.ClassNowFinalVisitor;
import com.github.maracas.visitors.ClassRemovedVisitor;
import com.github.maracas.visitors.MethodAddedToInterfaceVisitor;

import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class BrokenClass extends AbstractBrokenDeclaration {
	private final JApiClass jApiCls;
	private final CtTypeReference<?> clsRef;

	public BrokenClass(JApiClass cls, CtTypeReference<?> clsRef, JApiCompatibilityChange change) {
		super(change);
		this.jApiCls = cls;
		this.clsRef = clsRef;
	}

	@Override
	public CtReference getReference() {
		return clsRef;
	}

	@Override
	public BreakingChangeVisitor getVisitor() {
		return
			switch (change) {
				case CLASS_NO_LONGER_PUBLIC      -> null; // CLASS_LESS_ACCESSIBLE is a superset of CLASS_LESS_ACCESSIBLE; fix japicmp
				case CLASS_LESS_ACCESSIBLE       -> new ClassLessAccessibleVisitor(clsRef, jApiCls.getAccessModifier().getNewModifier().get());
				case CLASS_NOW_ABSTRACT          -> new ClassNowAbstractVisitor(clsRef);
				case CLASS_NOW_FINAL             -> new ClassNowFinalVisitor(clsRef);
				case CLASS_NOW_CHECKED_EXCEPTION -> new ClassNowCheckedExceptionVisitor(clsRef);
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedVisitor(clsRef);
				case CLASS_REMOVED               -> new ClassRemovedVisitor(clsRef);
				case METHOD_ADDED_TO_INTERFACE   -> new MethodAddedToInterfaceVisitor(clsRef);
				default -> null;
			};
	}
}
