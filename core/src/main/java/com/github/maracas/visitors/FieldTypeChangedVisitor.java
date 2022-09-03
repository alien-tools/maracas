package com.github.maracas.visitors;

import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all method return type changed issues in
 * client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Read accesses of the modified field where the expected type is not
 *      compatible with the new type. Example:
 *      <pre>
 *      int a = field; // field of type String
 *      </pre>
 * <li> Write accesses of the modified field where the expected type is not
 *      compatible with the new type. Only field assignments are considered.
 *      Example:
 *      <pre>
 *      self.field = 10; // field of type String
 *      </pre>
 * </ul>
 */
public class FieldTypeChangedVisitor extends BreakingChangeVisitor {
	/*
	  FIXME: Notes
	   - There must be a cleaner way than checking all possible usage contexts,
	       but I can't find it yet
	   - japicmp doesn't report a FIELD_TYPE_CHANGED when type parameters change,
	       e.g.: {@literal List<A>} to {@literal List<B>} (ofc it does for e.g.
	     {@literal List<A>} to {@literal Collection<A>})
	 */

	/**
	 * Spoon reference to the modified field.
	 */
	private final CtFieldReference<?> fRef;

	/**
	 * Spoon reference to the new type of the modified field.
	 */
	private final CtTypeReference<?> newType;

	/**
	 * Creates a FieldTypeChangedVisitor instance.
	 *
	 * @param fRef    the modified field
	 * @param newType the new type of the modified field
	 */
	public FieldTypeChangedVisitor(CtFieldReference<?> fRef, CtTypeReference<?> newType) {
		super(JApiCompatibilityChange.FIELD_TYPE_CHANGED);
		this.fRef = fRef;
		this.newType = newType;
	}

	@Override
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		if (fRef.equals(fieldRead.getVariable())) {
			CtTypeReference<?> expectedType = SpoonTypeHelpers.inferExpectedType(fieldRead.getParent());

			if (!SpoonTypeHelpers.isAssignableFrom(expectedType, newType))
				brokenUse(fieldRead, fieldRead.getVariable(), fRef, APIUse.FIELD_ACCESS);
		}
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		if (fRef.equals(fieldWrite.getVariable())) {
			// We should always be in an assignment
			CtAssignment<?, ?> enclosing = (CtAssignment<?, ?>) fieldWrite.getParent();
			CtTypeReference<?> assignedType = enclosing.getType();

			if (!SpoonTypeHelpers.isAssignableFrom(newType, assignedType))
				brokenUse(fieldWrite, fieldWrite.getVariable(), fRef, APIUse.FIELD_ACCESS);
		}
	}
}
