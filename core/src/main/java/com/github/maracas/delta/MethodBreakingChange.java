package com.github.maracas.delta;

import com.github.maracas.visitors.AnnotationDeprecatedAddedToMethodVisitor;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.ConstructorRemovedVisitor;
import com.github.maracas.visitors.MethodNowAbstractVisitor;
import com.github.maracas.visitors.MethodNowFinalVisitor;
import com.github.maracas.visitors.MethodRemovedVisitor;
import com.github.maracas.visitors.MethodReturnTypeChangedVisitor;
import japicmp.model.JApiBehavior;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

/**
 * Represents a method-level breaking change
 */
public class MethodBreakingChange extends AbstractBreakingChange {
	private final JApiBehavior jApiMethod;
	private final CtExecutableReference<?> mRef;

	public MethodBreakingChange(JApiBehavior method, CtExecutableReference<?> mRef, JApiCompatibilityChange change) {
		super(change);
		this.jApiMethod = Objects.requireNonNull(method);
		this.mRef = Objects.requireNonNull(mRef);
	}

	@Override
	public CtReference getReference() {
		return mRef;
	}

	@Override
	public BreakingChangeVisitor getVisitor() {
		return
			switch (change) {
				case CONSTRUCTOR_REMOVED         -> new ConstructorRemovedVisitor(mRef);
				case METHOD_REMOVED              -> new MethodRemovedVisitor(mRef);
				case METHOD_NOW_FINAL            -> new MethodNowFinalVisitor(mRef);
				case METHOD_NOW_ABSTRACT         -> new MethodNowAbstractVisitor(mRef);
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedToMethodVisitor(mRef);
				case METHOD_RETURN_TYPE_CHANGED  -> {
					String newTypeName = ((JApiMethod) jApiMethod).getReturnType().getNewReturnType();
					CtTypeReference<?> newType = mRef.getFactory().Type().createReference(newTypeName);
					yield new MethodReturnTypeChangedVisitor(mRef, newType);
				}
				// TODO: to be implemented
				case METHOD_LESS_ACCESSIBLE,
					METHOD_IS_STATIC_AND_OVERRIDES_NOT_STATIC,
					METHOD_NOW_STATIC,
					METHOD_NO_LONGER_STATIC,
					METHOD_ADDED_TO_INTERFACE,
					METHOD_NOW_THROWS_CHECKED_EXCEPTION,
					METHOD_NO_LONGER_THROWS_CHECKED_EXCEPTION,
					METHOD_ABSTRACT_NOW_DEFAULT,
					CONSTRUCTOR_LESS_ACCESSIBLE,
					METHOD_NOW_VARARGS,
					METHOD_NO_LONGER_VARARGS,
					METHOD_MOVED_TO_SUPERCLASS,
					METHOD_PARAMETER_GENERICS_CHANGED,
					METHOD_RETURN_TYPE_GENERICS_CHANGED,
					CLASS_GENERIC_TEMPLATE_CHANGED,
					CLASS_GENERIC_TEMPLATE_GENERICS_CHANGED -> null;
				default ->
					throw new IllegalStateException(this + " was somehow associated to a non-method-level breaking change: " + change);
			};
	}
}
