package com.github.maracas.util;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Helper in charge of verifying the compatibility between
 * types. It is of special help when dealing with widening,
 * narrowing, boxing, unboxing, and subtyping cases.
 */
public final class SpoonTypeHelpers {
	private SpoonTypeHelpers() {
	}

	/**
	 * Verifies if the given type {@code type} is the unboxed version of the
	 * reference type {@code ref}.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type is the unboxed version of the
	 * reference type; {@code false} otherwise
	 */
	public static boolean isUnboxedType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		return type.equals(ref.unbox());
	}

	/**
	 * Verifies if the given type {@code type} is the boxed version of the
	 * reference type {@code ref}.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type is the boxed version of the
	 * reference type; {@code false} otherwise
	 */
	public static boolean isBoxedType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		return type.unbox().equals(ref);
	}

	/**
	 * Verifies if a type narrows a reference type. It checks both for primitive
	 * and reference types. If the types are the same it returns {@code true}.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type narrows the reference type;
	 * {@code false} otherwise
	 */
	public static boolean isNarrowedType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		if (type.isPrimitive())
			return isWidenedPrimitiveType(type, ref);

		return type.equals(ref) || type.isSubtypeOf(ref);
	}

	/**
	 * Verifies if a type narrows a primitive type. If the types are the same it
	 * returns {@code true}. The implementation that verifies narrowing for
	 * primitive types is based on the <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.3">
	 * Java Language Specification (JLS) v10 chapter 5.1.3 and 5.1.4.</a>.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type narrows the primitive reference
	 * type; {@code false} otherwise
	 */
	public static boolean isNarrowedPrimitiveType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		if (type.isPrimitive()) {
			String typeName = type.getSimpleName();
			String refNAme = ref.getSimpleName();

			if (type.equals(ref))
				return true;
			else if (typeName.equals("byte"))
				return Set.of("char").contains(refNAme);
			else if (typeName.equals("short"))
				return Set.of("byte", "char").contains(refNAme);
			else if (typeName.equals("char"))
				return Set.of("byte", "short").contains(refNAme);
			else if (typeName.equals("int"))
				return Set.of("byte", "short", "char").contains(refNAme);
			else if (typeName.equals("long"))
				return Set.of("byte", "short", "char", "int").contains(refNAme);
			else if (typeName.equals("float"))
				return Set.of("byte", "short", "char", "int", "long").contains(refNAme);
			else if (typeName.equals("double"))
				return Set.of("byte", "short", "char", "int", "long", "float").contains(refNAme);
		}

		return false;
	}

	/**
	 * Verifies if a type widens a reference type. It checks both for primitive
	 * and reference types. If the types are the same it returns {@code true}.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type narrows the reference type;
	 * {@code false} otherwise
	 */
	public static boolean isWidenedType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		if (type.isPrimitive())
			return isWidenedPrimitiveType(type, ref);

		return type.equals(ref) || ref.isSubtypeOf(type);
	}

	/**
	 * Verifies if a type widens a primitive type. If the types are the same it
	 * returns {@code true}. The implementation that verifies narrowing for
	 * primitive types is based on the <a href="https://docs.oracle.com/javase/specs/jls/se10/html/jls-5.html#jls-5.1.2">
	 * Java Language Specification (JLS) v10 chapter 5.1.2</a>.
	 *
	 * @param type given type
	 * @param ref  reference type to checked against
	 * @return {@code true} if the given type narrows the primitive reference
	 * type; {@code false} otherwise
	 */
	public static boolean isWidenedPrimitiveType(CtTypeReference<?> type, CtTypeReference<?> ref) {
		if (type.isPrimitive()) {
			String typeName = type.getSimpleName();
			String refNAme = ref.getSimpleName();

			if (type.equals(ref))
				return true;
			else if (typeName.equals("byte"))
				return Set.of("short", "int", "long", "float", "double").contains(refNAme);
			else if (typeName.equals("short"))
				return Set.of("int", "long", "float", "double").contains(refNAme);
			else if (typeName.equals("char"))
				return Set.of("int", "long", "float", "double").contains(refNAme);
			else if (typeName.equals("int"))
				return Set.of("long", "float", "double").contains(refNAme);
			else if (typeName.equals("long"))
				return Set.of("float", "double").contains(refNAme);
			else if (typeName.equals("float"))
				return Set.of("double").contains(refNAme);
			else if (typeName.equals("double"))
				return Set.of("byte", "short", "char", "int", "long", "float").contains(refNAme);
		}

		return false;
	}

	/**
	 * Verifies if the given type is assignable to the expected type. Boxing and
	 * unboxing cases are considered as {@code false} cases (non-assignable).
	 *
	 * @param expected expected type
	 * @param given    given type
	 * @return {@code true} if the given type is assignable to the expected type;
	 * {@code false} otherwise
	 */
	public static boolean isAssignableFromOverride(CtTypeReference<?> expected, CtTypeReference<?> given) {
		if (isBoxedType(expected, given) || isUnboxedType(expected, given) || isWidenedPrimitiveType(expected, given))
			return false;
		else
			return isAssignableFrom(expected, given);
	}

	/**
	 * Couldn't find a built-in utility to check all cases.
	 * This implementation most likely messes up.
	 */
	public static boolean isAssignableFrom(CtTypeReference<?> expected, CtTypeReference<?> given) {
		if (given == null || expected == null)
			return false;

		if (expected.equals(given))
			return true;

		// We can pass a subtype => only succeeds if given and expected are classes
		// or interfaces, and given <: expected
		if (given.isSubtypeOf(expected))
			return true;

		// If we expect a primitive, either we can widen the given primitive,
		// or it is a compatible boxed type
		if (expected.isPrimitive())
			return primitivesAreCompatible(expected, given.unbox()); // No helper for that!?

			// If it's a boxed type
		else if (!expected.equals(expected.unbox()))
			return primitivesAreCompatible(expected.unbox(), given.unbox());

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

		// If we expect a class/interface/enum, we already checked for subtyping,
		// so that's a no
		else return false;
	}

	/**
	 * Verifies if a given primitive type is compatible with
	 * another one. The verification is based on the JLS SE8.
	 *
	 * @param expected Expected primitive type
	 * @param given    Given primitive type
	 * @return <code>true</code> if the given type can be
	 * widened into the expected type; <code>false</code>
	 * otherwise.
	 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">
	 * Widening Primitive Conversion</a>
	 */
	private static boolean primitivesAreCompatible(CtTypeReference<?> expected, CtTypeReference<?> given) {
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
			return Objects.equals("double", expectedName);

		return false;
	}

	public static boolean isSubtype(CtTypeReference<?> typeRef, CtTypeReference<?> superRef) {
		if (typeRef != null)
			return isSubtype(Set.of(typeRef), superRef);
		else
			return false;
	}

	/**
	 * Verifies if a set of type references are subtypes of the typeRef.
	 * TODO: Don't see the point of this method anymore. Might need to go away.
	 *
	 * @param typeRefs set of type references
	 * @param superRef reference super type
	 * @return <code>true</code> if any of the types is a subtype of the clsRef;
	 * <code>false</code> otherwise.
	 */
	public static boolean isSubtype(Set<CtTypeReference<?>> typeRefs, CtTypeReference<?> superRef) {
		for (CtTypeReference<?> ref : typeRefs) {
			if (ref == null || ref.getTypeDeclaration() == null)
				return false;

			if (ref.equals(superRef))
				return true;

			if ((ref.getTypeDeclaration().isAbstract() || ref.isInterface())
				&& ref.isSubtypeOf(superRef)) {
				// FIXME: interfaces extending other interfaces are not considered
				// by the isSubtypeOf() method
				Set<CtTypeReference<?>> supers = new HashSet<>(ref.getSuperInterfaces());
				supers.add(ref.getSuperclass());
				return isSubtype(supers, superRef);
			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * Verifies if a set of {@link CtTypeReference} objects declare at least one
	 * abstract method that has not been implemented along the type hierarchy.
	 *
	 * @param types {@link CtTypeReference} objects to analyze
	 * @return {@code true} if there is at least one unimplemented abstract
	 * method declared by the types passed as parameter; otherwise
	 * {@code false}
	 */
	public static boolean haveUnimplAbstractMethods(Set<CtTypeReference<?>> types) {
		for (CtTypeReference<?> sup : types) {
			CtType<?> decl = sup.getTypeDeclaration();
			if (decl == null)
				return true; // Over-approximate
			else
				for (CtExecutableReference<?> e : sup.getAllExecutables()) {
					CtBlock<?> body = e.getExecutableDeclaration().getBody();
					CtExecutableReference<?> overriden = e.getOverridingExecutable();

					// Broken use if there is an abstract supermethod with
					// no concrete implementation
					if (body == null && overriden == null)
						return true;
				}
		}
		return false;
	}

	// Oof
	public static CtTypeReference<?> inferExpectedType(CtElement e) {
		if (e instanceof CtTypedElement<?> elem)
			return elem.getType();
		else if (e instanceof CtLoop)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtIf)
			return e.getFactory().Type().booleanPrimitiveType();
		else if (e instanceof CtThrow thrw)
			return thrw.getThrownExpression().getType();
		else if (e instanceof CtReturn<?> retrn)
			return retrn.getReturnedExpression().getType();
		else if (e instanceof CtSynchronized sync)
			return sync.getExpression().getType();
		else if (e instanceof CtAssignment<?, ?> assign)
			return assign.getType();
		else if (e instanceof CtBlock)
			return null;

		throw new IllegalArgumentException("Unhandled enclosing type " + e.getClass());
	}
}
