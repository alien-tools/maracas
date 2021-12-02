package com.github.maracas.delta;

import java.util.Set;
import java.util.stream.Collectors;

import com.github.maracas.visitors.AnnotationDeprecatedAddedVisitor;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ClassLessAccessibleVisitor;
import com.github.maracas.visitors.ClassNowAbstractVisitor;
import com.github.maracas.visitors.ClassNowCheckedExceptionVisitor;
import com.github.maracas.visitors.ClassNowFinalVisitor;
import com.github.maracas.visitors.ClassRemovedVisitor;
import com.github.maracas.visitors.InterfaceAddedVisitor;
import com.github.maracas.visitors.MethodAddedToInterfaceVisitor;
import com.github.maracas.visitors.SuperclassAddedVisitor;

import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiSuperclass;
import japicmp.util.Optional;
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
				case INTERFACE_ADDED             -> {
					Set<CtTypeReference<?>> intersRef = jApiCls.getInterfaces().stream()
						.filter(i -> i.getChangeStatus().equals(JApiChangeStatus.NEW))
						.map(i -> clsRef.getFactory().Type().createReference(i.getFullyQualifiedName()))
						.collect(Collectors.toSet());
					yield new InterfaceAddedVisitor(clsRef, intersRef);
				}
				case METHOD_ADDED_TO_INTERFACE   -> new MethodAddedToInterfaceVisitor(clsRef);
				case SUPERCLASS_ADDED            -> {
					JApiSuperclass superclass = jApiCls.getSuperclass();
					if (superclass != null) {
						CtTypeReference<?> superRef = clsRef.getFactory().Type().createReference(superclass.getSuperclassNew());
						yield new SuperclassAddedVisitor(clsRef, superRef);
					} else {
						yield null;
					}			
				}
				default -> null;
			};
	}
}
