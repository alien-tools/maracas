package org.swat.maracas.spoon.visitors;

import java.util.Set;

import org.swat.maracas.spoon.delta.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtArrayTypeReference;
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
 * 		e.g.: List<A> to List<B> (ofc it does for e.g. List<A> to Collection<A>)
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
			CtTypeReference<?> expectedType = inferExpectedType(fieldRead.getParent());

			if (!isAssignableFrom(expectedType, newType))
				detection(fieldRead, fieldRead.getVariable(), fRef, APIUse.FIELD_ACCESS);
		}
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		if (fRef.equals(fieldWrite.getVariable())) {
			// We should always be in an assignment
			CtAssignment<?, ?> enclosing = (CtAssignment<?, ?>) fieldWrite.getParent();
			CtTypeReference<?> assignedType = enclosing.getType();

			if (!isAssignableFrom(newType, assignedType))
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

	/**
	 * Couldn't find a built-in utility to check all cases.
	 * This implementation most likely messes up.
	 */
	private boolean isAssignableFrom(CtTypeReference<?> expected, CtTypeReference<?> given) {
		if (expected.equals(given))
			return true;

		// We can pass a subtype => only succeeds if given and expected are classes
		// or interfaces, and given <: expected
		if (given.isSubtypeOf(expected))
			return true;

		// If we expect a primitive, either we can widen the given primitive,
		// or it is a compatible boxed type
		if (expected.isPrimitive()) {
			return primitivesAreCompatible(expected, given.unbox()); // No helper for that!?
		}

		// If it's a boxed type
		else if (!expected.equals(expected.unbox())) {
			return primitivesAreCompatible(expected.unbox(), given.unbox());
		}

		// If we expect an array, only compatible type is an array of a subtype
		// FIXME: this should account for multidimensional arrays
		else if (expected.isArray()) {
			if (given.isArray()) {
				CtArrayTypeReference<?> expectedArrayType = (CtArrayTypeReference<?>) expected;
				CtArrayTypeReference<?> givenArrayType = (CtArrayTypeReference<?>) given;

				return givenArrayType.getArrayType().isSubtypeOf(expectedArrayType.getArrayType());
			}

			return false;
		}

		// If we expect a class/interface, we already checked for subtyping,
		// so that's a no
		else if (expected.isClass() || expected.isInterface())
			return false;

		// If we have classes/interfaces, check subtyping. Otherwise if given is
		// not a class/interface, check for boxing
//		else if (expected.isClass() || expected.isInterface()) {
//			System.out.println("Checking boxing of " + expected + " and " + given);
//			if (given.isClass() || given.isInterface())
//				return given.isSubtypeOf(expected);
//
//			System.out.println("Checking boxing of " + expected + " and " + given);
//			return Objects.equals(expected.box(), given.box());
//		}

		throw new RuntimeException(
			"Unhandled type conversion case (" + expected + " <: " + given + ")");
	}

	private boolean primitivesAreCompatible(CtTypeReference<?> expected, CtTypeReference<?> given) {
		String expectedName = expected.getSimpleName();
		String givenName = given.getSimpleName();

		if (expectedName.equals(givenName))
			return true;

		// https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2
		if (givenName.equals("byte"))
			return Set.of("short", "int", "long", "float", "double").contains(expectedName);
		if (givenName.equals("short"))
			return Set.of("int", "long", "float", "double").contains(expectedName);
		if (givenName.equals("char"))
			return Set.of("int", "long", "float", "double").contains(expectedName);
		if (givenName.equals("int"))
			return Set.of("long", "float", "double").contains(expectedName);
		if (givenName.equals("long"))
			return Set.of("float", "double").contains(expectedName);
		if (givenName.equals("float"))
			return Set.of("double").contains(expectedName);

		return false;
	}
}
