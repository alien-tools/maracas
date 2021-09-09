package com.github.maracas;

import javassist.CtMethod;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class SpoonHelper {
	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (!(parent.getPosition() instanceof NoSourcePosition))
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	public static boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtMethod japiMethod) {
		return
			japiMethod.getName().concat(japiMethod.getSignature()).startsWith(spoonMethod.getSignature());
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
}