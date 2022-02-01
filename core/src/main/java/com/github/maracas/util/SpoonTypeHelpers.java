package com.github.maracas.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Helper in charge of verifying the compatibility between
 * types. It is of special help when dealing with widening,
 * narrowing, boxing, unboxing, and subtyping cases.
 */
public class SpoonTypeHelpers {
    /**
     * Private constructor of the SpoonTypeHelpers.
     *
     * The class cannot have any instance. Its declarations must be used in a
     * static manner.
     */
    private SpoonTypeHelpers() {}

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

    /**
     * Verifies if a set of type references are subtypes of the clsRef.
     * @param superRefs set of type references
     * @param typeRef   reference super type
     * @return          <code>true</code> if any of the types is a
     *                  subtype of the clsRef;
     *                  <code>false</code> otherwise.
     */
    public static boolean isSubtype(Set<CtTypeReference<?>> superRefs, CtTypeReference<?> typeRef) {
        for (CtTypeReference<?> superRef : superRefs) {
            if (superRef == null || superRef.getTypeDeclaration() == null)
                return false;

            if (superRef.equals(typeRef))
                return true;

            if ((superRef.getTypeDeclaration().isAbstract() || superRef.isInterface())
                && superRef.isSubtypeOf(typeRef)) {
                // FIXME: interfaces extending other interfaces are not considered
                // by the isSubtypeOf() method
                Set<CtTypeReference<?>> clsSupers = new HashSet<>(superRef.getSuperInterfaces());
                clsSupers.add(superRef.getSuperclass());
                return isSubtype(clsSupers, typeRef);
            } else {
                return false;
            }
        }

        return false;
    }
}
