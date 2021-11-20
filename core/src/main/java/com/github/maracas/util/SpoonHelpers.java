package com.github.maracas.util;

import javassist.CtBehavior;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtThrow;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class SpoonHelpers {

	private SpoonHelpers() {}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (!(parent.getPosition() instanceof NoSourcePosition))
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	/**
	 * Verifies if the signature of a Spoon method (CtExecutableReference)
	 * is equivalent to the one of the JApiCmp method (CtBehavior).
	 * @param spoonMethod the Spoon method
	 * @param japiMethod  The JapiCmp method
	 * @return            <code>true</code> if the methods have the same 
	 *                    signature; <code>false</code> otherwise.
	 */
	public static boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtBehavior japiMethod) {
		String japiMethName = "";

		if (spoonMethod.isConstructor() && japiMethod.getLongName().contains("$")) {  // Inner class constructor
			String ln = japiMethod.getLongName();
			String outerCN = ln.substring(0, ln.indexOf("$"));
			japiMethName = ln.replaceAll(String.format("\\(%s,?", outerCN), "(");
		} else if (spoonMethod.isConstructor()) {                                     // Regular constructor
			japiMethName = japiMethod.getLongName();
		} else {                                                                      // Regular method
			japiMethName = japiMethod.getName().concat(japiMethod.getSignature());
		}

		return japiMethName.startsWith(spoonMethod.getSignature());
	}

	public static String fullyQualifiedName(CtReference ref) {
		String fqn = "";
		if (ref instanceof CtTypeReference<?> tRef)
			fqn = tRef.getQualifiedName();
		else if (ref instanceof CtExecutableReference<?> eRef)
			fqn = eRef.getDeclaringType().getQualifiedName().concat(".").concat(eRef.getSignature());
		else if (ref instanceof CtFieldReference<?> fRef)
			fqn = fRef.getDeclaringType().getQualifiedName().concat(".").concat(fRef.getSimpleName());

		return fqn;
	}

	public static String getEnclosingPkgName(CtElement e) {
		CtPackage enclosing = e.getParent(CtPackage.class);
		return
			enclosing != null ?
				enclosing.getQualifiedName() :
				CtPackage.TOP_LEVEL_PACKAGE_NAME;
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

		// FIXME: CtSwitch not supported yet

		throw new RuntimeException("Unhandled enclosing type " + e.getClass());
	}
}
