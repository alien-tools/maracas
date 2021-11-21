package com.github.maracas.util;

import java.util.Set;

import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Helper in charge of verifying the compatibility between
 * types. It is of special help when dealing with widening,
 * narrowing, boxing, unboxing, and subtyping cases.
 */
public class SpoonTypeHelpers {

	private SpoonTypeHelpers() {}

	/**
	 * Couldn't find a built-in utility to check all cases.
	 * This implementation most likely messes up.
	 */
	public static boolean isAssignableFrom(CtTypeReference<?> expected, CtTypeReference<?> given) {
		// We can pass a subtype => only succeeds if given and expected are classes
		// or interfaces, and given <: expected
		if (expected.equals(given) || given.isSubtypeOf(expected))
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
		//			else if (expected.isClass() || expected.isInterface()) {
		//				System.out.println("Checking boxing of " + expected + " and " + given);
		//				if (given.isClass() || given.isInterface())
		//					return given.isSubtypeOf(expected);
		//
		//				System.out.println("Checking boxing of " + expected + " and " + given);
		//				return Objects.equals(expected.box(), given.box());
		//			}

		throw new RuntimeException(
				"Unhandled type conversion case (" + expected + " <: " + given + ")");
	}

	/**
	 * Verifies if a given primitive type is compatible with 
	 * another one. The verification is based on the JLS SE8.
	 * @see            <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">
	 *                 Widening Primitive Conversion</a>
	 * @param expected Expected primitive type
	 * @param given    Given primitive type
	 * @return         <code>true</code> if the given type can be 
	 *                 widened into the expected type; <code>false</code>
	 *                 otherwise.
	 */
	private static boolean primitivesAreCompatible(CtTypeReference<?> expected, CtTypeReference<?> given) {
		String expectedName = expected.getSimpleName();
		String givenName = given.getSimpleName();

		if (expectedName.equals(givenName))
			return true;

		if (givenName.equals("byte"))
			return Set.of("short", "int", "long", "float", "double").contains(expectedName);
		if (givenName.equals("short") || givenName.equals("char"))
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
