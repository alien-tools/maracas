package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Visitor in charge of gathering method removed issues in client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Invocations to the now-removed method. Example:
 *      <pre>
 *      var a = method();
 *      </pre>
 * <li> Methods overriding the now-removed method. Example:
 *      <pre>
 *      &#64;Override
 *      public void method() { return; }
 *      </pre>
 * </ul>
 */
public class MethodRemovedVisitor extends MethodReferenceVisitor {
	/**
	 * Creates a MethodRemovedVisitor instance.
	 *
	 * @param mRef the now-removed method
	 */
	public MethodRemovedVisitor(CtExecutableReference<?> mRef) {
		super(mRef, JApiCompatibilityChange.METHOD_REMOVED);
	}
}
