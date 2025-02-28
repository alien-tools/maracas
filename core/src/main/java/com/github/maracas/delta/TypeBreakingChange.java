package com.github.maracas.delta;

import com.github.maracas.visitors.AnnotationDeprecatedAddedToClassVisitor;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ClassLessAccessibleVisitor;
import com.github.maracas.visitors.ClassNowAbstractVisitor;
import com.github.maracas.visitors.ClassNowCheckedExceptionVisitor;
import com.github.maracas.visitors.ClassNowFinalVisitor;
import com.github.maracas.visitors.ClassRemovedVisitor;
import com.github.maracas.visitors.InterfaceAddedVisitor;
import com.github.maracas.visitors.InterfaceRemovedVisitor;
import com.github.maracas.visitors.MethodAddedToInterfaceVisitor;
import com.github.maracas.visitors.SuperclassAddedVisitor;
import com.github.maracas.visitors.SuperclassRemovedVisitor;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiSuperclass;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Represents a type-level breaking change (class, interface, enum)
 */
public class TypeBreakingChange extends AbstractBreakingChange {
	private final JApiClass jApiCls;
	private final CtTypeReference<?> clsRef;

	public TypeBreakingChange(JApiClass cls, CtTypeReference<?> clsRef, JApiCompatibilityChange change) {
		super(change);
		this.jApiCls = Objects.requireNonNull(cls);
		this.clsRef = Objects.requireNonNull(clsRef);
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
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedToClassVisitor(clsRef);
				case CLASS_REMOVED               -> new ClassRemovedVisitor(clsRef);
				case METHOD_ADDED_TO_INTERFACE   -> new MethodAddedToInterfaceVisitor(clsRef);
				case INTERFACE_ADDED -> {
					Set<CtTypeReference<?>> newInterfaces = jApiCls.getInterfaces().stream()
						.filter(i -> i.getChangeStatus().equals(JApiChangeStatus.NEW))
						.map(i -> clsRef.getFactory().Type().createReference(i.getFullyQualifiedName()))
						.collect(toSet());
					yield new InterfaceAddedVisitor(clsRef, newInterfaces);
				}
				case INTERFACE_REMOVED -> {
					Set<CtTypeReference<?>> oldInterfaces = jApiCls.getInterfaces().stream()
						.filter(i -> i.getChangeStatus().equals(JApiChangeStatus.REMOVED))
						.map(i -> clsRef.getFactory().Type().createReference(i.getFullyQualifiedName()))
						.collect(toSet());
					yield new InterfaceRemovedVisitor(clsRef, oldInterfaces);
				}
				case SUPERCLASS_ADDED -> {
					JApiSuperclass superClass = jApiCls.getSuperclass();
					CtTypeReference<?> newSuper = clsRef.getFactory().Type().createReference(superClass.getSuperclassNew());
					yield new SuperclassAddedVisitor(clsRef, newSuper);
				}
				case SUPERCLASS_REMOVED -> {
					JApiSuperclass superClass = jApiCls.getSuperclass();
					CtTypeReference<?> oldSuper = clsRef.getFactory().Type().createReference(superClass.getSuperclassOld());
					yield new SuperclassRemovedVisitor(clsRef, oldSuper);
				}
				// TODO: To be implemented
				case METHOD_ABSTRACT_ADDED_TO_CLASS,
					METHOD_NEW_DEFAULT,
					CLASS_TYPE_CHANGED,
					CLASS_GENERIC_TEMPLATE_CHANGED,
					CLASS_GENERIC_TEMPLATE_GENERICS_CHANGED -> null;
				default ->
					throw new IllegalStateException(this + " was somehow associated to a non-class-level breaking change: " + change);
			};
	}
}
