package com.github.maracas.util;

import japicmp.model.JApiConstructor;
import japicmp.model.JApiMethod;
import japicmp.model.JApiParameter;
import javassist.CtBehavior;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class SpoonHelpers {
	private SpoonHelpers() {
	}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (!(parent.getPosition() instanceof NoSourcePosition))
				return parent;
		} while ((parent = parent.getParent()) != null);
		return null;
	}

	public static String buildSpoonSignature(JApiMethod m) {
		String returnType = m.getReturnType().getOldReturnType();
		if (returnType.equals("n.a."))
			returnType = "void";
		String type = m.getjApiClass().getFullyQualifiedName();
		String name = m.getName();
		String params = m.getParameters().stream().map(JApiParameter::getType).collect(joining(","));
		return "%s %s#%s(%s)".formatted(returnType, type, name, params);
	}

	public static String buildSpoonSignature(JApiConstructor cons) {
		String type = cons.getjApiClass().getFullyQualifiedName();
		List<JApiParameter> params = cons.getParameters();
		if (cons.getName().contains("$") && !params.isEmpty()) {
			String firstParam = params.get(0).getType();
			String containingCls = cons.getjApiClass().getFullyQualifiedName();
			String outerCls = containingCls.substring(0, containingCls.lastIndexOf("$"));

			if (firstParam.equals(outerCls)) // anonymous class or non-static inner class
				params.remove(0);
		}
		return " %s#<init>(%s)".formatted(type, params.stream().map(JApiParameter::getType).collect(joining(",")));
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

	/**
	 * Verifies if a Spoon CtElement is implicit. References a specific
	 * implementation of the isImplicit() Spoon method given the type of
	 * declaration the input element represents.
	 *
	 * @param elem the CtElement to verify
	 * @return <code>true</code> if the element is implicit;
	 * <code>false</code> otherwise.
	 */
	public static boolean isImplicit(CtElement elem) {
		if (elem instanceof CtConstructor<?> cons)
			return cons.isImplicit();
		else if (elem instanceof CtField<?> field)
			return field.isImplicit();
		else if (elem instanceof CtMethod<?> meth)
			return meth.isImplicit();
		else if (elem instanceof CtTypeAccess<?> typeAcc)
			return typeAcc.isImplicit();
			// Default to CtElement isImplicit() method. Other cases might be
			// missing.
		else
			return elem.isImplicit();
	}

	/**
	 * Verifies if the signature of a Spoon method (CtExecutableReference)
	 * is equivalent to the one of the JApiCmp method (CtBehavior).
	 * <p>
	 * FIXME: This method must disappear once we solve the issue with the
	 * constructor signature.
	 *
	 * @param spoonMethod the Spoon method
	 * @param japiMethod  The JapiCmp method
	 * @return <code>true</code> if the methods have the same
	 * signature; <code>false</code> otherwise.
	 * @deprecated
	 */
	@Deprecated
	public static boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtBehavior japiMethod) {
		String japiMethName;

		if (spoonMethod.isConstructor() && japiMethod.getName().contains("$")) {  // Inner class constructor
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
}
