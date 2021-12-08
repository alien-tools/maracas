package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.util.SpoonTypeHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of FIELD_TYPE_CHANGED are:
 * 	- Type-incompatible uses of the changed field in an expression, i.e. anywhere
 * 	where we expect a value of a specific type and might now get a different one
 *
 * Notes:
 * 	- There must be a cleaner way than checking all possible usage contexts,
 * 		but I can't find it yet
 * 	- japicmp doesn't report a FIELD_TYPE_CHANGED when type parameters change,
 * 		e.g.: {@literal List<A>} to {@literal List<B>} (ofc it does for e.g.
 * 	  {@literal List<A>} to {@literal Collection<A>})
 */
public class FieldTypeChangedVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;
	private final CtTypeReference<?> newType;

	public FieldTypeChangedVisitor(CtFieldReference<?> fRef, CtTypeReference<?> newType) {
		super(JApiCompatibilityChange.FIELD_TYPE_CHANGED);
		this.fRef = fRef;
		this.newType = newType;
	}

	@Override
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		if (fRef.equals(fieldRead.getVariable())) {
			CtTypeReference<?> expectedType = SpoonHelpers.inferExpectedType(fieldRead.getParent());

			if (!SpoonTypeHelpers.isAssignableFrom(expectedType, newType))
				detection(fieldRead, fieldRead.getVariable(), fRef, APIUse.FIELD_ACCESS);
		}
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		if (fRef.equals(fieldWrite.getVariable())) {
			// We should always be in an assignment
			CtAssignment<?, ?> enclosing = (CtAssignment<?, ?>) fieldWrite.getParent();
			CtTypeReference<?> assignedType = enclosing.getType();

			if (!SpoonTypeHelpers.isAssignableFrom(newType, assignedType))
				detection(fieldWrite, fieldWrite.getVariable(), fRef, APIUse.FIELD_ACCESS);
		}
	}

	// Oof
	private CtTypeReference<?> inferExpectedType(CtElement e) {
		if (e instanceof CtTypedElement<?> elem)
			return elem.getType();
		else if (e instanceof CtLoop)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtIf)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtThrow thrw)
			return thrw.getThrownExpression().getType();

		// FIXME: CtSwitch not supported yet

		throw new RuntimeException("Unhandled enclosing type " + e.getClass());
	}
}
