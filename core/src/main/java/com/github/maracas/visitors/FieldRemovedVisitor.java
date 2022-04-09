package com.github.maracas.visitors;

import com.github.maracas.brokenuse.APIUse;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
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
public class FieldRemovedVisitor extends BreakingChangeVisitor {
	/**
	 * Spoon reference to the removed field.
	 */
	private final CtFieldReference<?> fRef;

	/**
	 * Creates a FieldRemovedVisitor instance.
	 *
	 * @param fRef the now-removed field
	 */
	public FieldRemovedVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_REMOVED);
		this.fRef = fRef;
	}

	@Override
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		visitCtFieldAccess(fieldRead);
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		visitCtFieldAccess(fieldWrite);
	}

	/**
	 * Visits a field access element and adds a new broken use if the accessed
	 * field corresponds to the now-removed field.
	 *
	 * @param <T>         type of the field
	 * @param fieldAccess field access
	 */
	private <T> void visitCtFieldAccess(CtFieldAccess<T> fieldAccess) {
		if (fRef.equals(fieldAccess.getVariable()))
			brokenUse(fieldAccess, fieldAccess.getVariable(), fRef, APIUse.FIELD_ACCESS);
	}
}
