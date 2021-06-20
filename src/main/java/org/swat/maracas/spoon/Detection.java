package org.swat.maracas.spoon;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;

public record Detection (
	CtElement element,
	CtElement usedApiElement,
	CtReference source,
	APIUse use,
	JApiCompatibilityChange change
) {
	@Override
	public String toString() {
		return """
		[%s]
			Element: %s (%s:%d)
			Used:    %s
			Source:  %s
			Use:     %s\
		""".formatted(
			change,
			element instanceof CtNamedElement namedElement ? namedElement.getSimpleName() :	element.toString(),
			element.getPosition() instanceof NoSourcePosition ? "unknown" : element.getPosition().getFile().getName(),
			element.getPosition() instanceof NoSourcePosition ? -1 : element.getPosition().getLine(),
			usedApiElement instanceof CtNamedElement namedUsedApiElement ? namedUsedApiElement.getSimpleName() : usedApiElement.toString(),
			source,
			use
		);
	}
}
