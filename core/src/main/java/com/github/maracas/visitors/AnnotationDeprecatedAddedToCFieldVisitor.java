package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtFieldReference;

/**
 * Visitor in charge of gathering field deprecated issues in client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Any reference to the now-deprecated field. Example:
 *      <pre>
 *      var a = field;
 *      self.field = 10;
 *      </pre>
 * </ul>
 */
public class AnnotationDeprecatedAddedToCFieldVisitor extends FieldReferenceVisitor {
	/**
	 * Creates a {@link AnnotationDeprecatedAddedToCFieldVisitor} instance.
	 *
	 * @param fRef the now-deprecated field
	 */
	public AnnotationDeprecatedAddedToCFieldVisitor(CtFieldReference<?> fRef) {
		super(fRef, JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED);
	}
}
