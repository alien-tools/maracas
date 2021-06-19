package org.swat.maracas.spoon;

import spoon.reflect.declaration.CtElement;

public class SpoonHelper {
	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (parent.getPosition().getFile() != null)
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}
}
