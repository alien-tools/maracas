package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Visitor in charge of gathering method deprecated issues in client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Invocations to the now-deprecated method. Example:
 *      <pre>
 *      var a = method();
 *      </pre>
 * <li> Methods overriding the now-deprecated method. Example:
 *      <pre>
 *      &#64;Override
 *      public void method() { return; }
 *      </pre>
 * </ul>
 */
public class AnnotationDeprecatedAddedToMethodVisitor extends MethodReferenceVisitor {
	/**
	 * Creates a {@link AnnotationDeprecatedAddedToMethodVisitor} instance.
	 *
	 * @param mRef the now-deprecated method
	 */
	public AnnotationDeprecatedAddedToMethodVisitor(CtExecutableReference<?> mRef) {
		super(mRef, JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED);
	}
}
