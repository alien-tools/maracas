package org.swat.maracas.spoon;

import javassist.CtMethod;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;

public class SpoonHelper {
	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (parent.getPosition().getFile() != null)
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	public static boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtMethod japiMethod) {
		return
			japiMethod.getName().concat(japiMethod.getSignature()).startsWith(spoonMethod.getSignature());
	}
}
