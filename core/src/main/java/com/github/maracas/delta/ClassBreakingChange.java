package com.github.maracas.delta;

import com.github.maracas.visitors.*;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiSuperclass;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;
import java.util.stream.Collectors;

public class ClassBreakingChange extends AbstractBreakingChange {
	private final JApiClass jApiCls;
	private final CtTypeReference<?> clsRef;

	public ClassBreakingChange(JApiClass cls, CtTypeReference<?> clsRef, JApiCompatibilityChange change) {
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
				case CLASS_LESS_ACCESSIBLE       -> new ClassLessAccessibleVisitor(clsRef, jApiCls.getAccessModifier().getNewModifier().get());
				case CLASS_NOW_ABSTRACT          -> new ClassNowAbstractVisitor(clsRef);
				case CLASS_NOW_FINAL             -> new ClassNowFinalVisitor(clsRef);
				case CLASS_NOW_CHECKED_EXCEPTION -> new ClassNowCheckedExceptionVisitor(clsRef);
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedVisitor(clsRef);
				case CLASS_REMOVED               -> new ClassRemovedVisitor(clsRef);
				case METHOD_ADDED_TO_INTERFACE   -> new MethodAddedToInterfaceVisitor(clsRef);
				case INTERFACE_ADDED             -> {
					Set<CtTypeReference<?>> newInterfaces = jApiCls.getInterfaces().stream()
						.filter(i -> i.getChangeStatus().equals(JApiChangeStatus.NEW))
						.map(i -> clsRef.getFactory().Type().createReference(i.getFullyQualifiedName()))
						.collect(Collectors.toSet());
					yield new InterfaceAddedVisitor(clsRef, newInterfaces);
				}
				case INTERFACE_REMOVED           -> {
					Set<CtTypeReference<?>> oldInterfaces = jApiCls.getInterfaces().stream()
						.filter(i -> i.getChangeStatus().equals(JApiChangeStatus.REMOVED))
						.map(i -> clsRef.getFactory().Type().createReference(i.getFullyQualifiedName()))
						.collect(Collectors.toSet());
					yield new InterfaceRemovedVisitor(clsRef, oldInterfaces);
				}
				case SUPERCLASS_ADDED            -> {
					JApiSuperclass superClass = jApiCls.getSuperclass();
					CtTypeReference<?> newSuper = clsRef.getFactory().Type().createReference(superClass.getSuperclassNew());
					yield new SuperclassAddedVisitor(clsRef, newSuper);
				}
				case SUPERCLASS_REMOVED          -> {
					JApiSuperclass superClass = jApiCls.getSuperclass();
					CtTypeReference<?> oldSuper = clsRef.getFactory().Type().createReference(superClass.getSuperclassOld());
					yield new SuperclassRemovedVisitor(clsRef, oldSuper);
				}
				default -> null; // FIXME: should eventually disappear
			};
	}
}
