package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtFieldReference;

/**
 * Visitor in charge of gathering field removed issues in client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Read accesses of the now-removed field. Example:
 *      <pre>
 *      var a = field;
 *      </pre>
 * <li> Write accesses of the now-removed field. Example:
 *      <pre>
 *      self.field = 10;
 *      </pre>
 * </ul>
 */
public class FieldRemovedVisitor extends FieldReferenceVisitor {
	/**
	 * Creates a FieldRemovedVisitor instance.
	 *
	 * @param fRef the now-removed field
	 */
	public FieldRemovedVisitor(CtFieldReference<?> fRef) {
		super(fRef, JApiCompatibilityChange.FIELD_REMOVED);
	}
}
