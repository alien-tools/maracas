package com.github.maracas.brokenuse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.maracas.util.CtElementSerializer;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;

import java.util.Objects;

/**
 * A broken use is a source code location in client code that is impacted by a breaking change.
 *
 * @param element        the impacted {@link CtElement} in the client's AST
 * @param usedApiElement the {@link CtElement} in the library's code that is directly used by the {@link #element}
 * @param source         the original declaration affected by a breaking change in the library's code
 *                       (often times the same as {@link #usedApiElement})
 * @param use            the kind of use relationship between the {@link #element} and {@link #usedApiElement}
 * @param change         the kind of breaking change affecting {@link #source}
 */
public record BrokenUse(
	@JsonSerialize(using = CtElementSerializer.class)
	CtElement element,
	@JsonSerialize(using = CtElementSerializer.class)
	CtElement usedApiElement,
	@JsonSerialize(using = ToStringSerializer.class)
	CtReference source,
	APIUse use,
	JApiCompatibilityChange change
) {
	public BrokenUse {
		Objects.requireNonNull(element);
		Objects.requireNonNull(usedApiElement);
		Objects.requireNonNull(source);
		Objects.requireNonNull(use);
	}

	@Override
	public String toString() {
		return """
			[%s]
			    Element: %s (%s:%d)
			    Used:    %s
			    Source:  %s
			    Use:     %s
			""".formatted(
			change,
			element instanceof CtNamedElement namedElement ? namedElement.getSimpleName() : element.toString(),
			element.getPosition() instanceof NoSourcePosition ? "unknown" : element.getPosition().getFile().getName(),
			element.getPosition() instanceof NoSourcePosition ? -1 : element.getPosition().getLine(),
			usedApiElement instanceof CtNamedElement namedUsed ? namedUsed.getSimpleName() : usedApiElement.toString(),
			source,
			use
		);
	}

	@Override
	public int hashCode() {
		// CtElement::equals/hashCode() do not check the position
		return Objects.hash(
			element.getPosition().toString(),
			element.getShortRepresentation(),
			usedApiElement.getPosition().toString(),
			source.getPosition().toString(),
			use,
			change);
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (that == null)
			return false;
		if (getClass() != that.getClass())
			return false;
		BrokenUse other = (BrokenUse) that;
		return comparePositions(element, other.element)
			&& comparePositions(usedApiElement, other.usedApiElement)
			&& comparePositions(source, other.source)
			&& use == other.use
			&& change == other.change;
	}

	/**
	 * Compares two Spoon positions based on their string representation.
	 *
	 * @param elem1 first Spoon element to compare
	 * @param elem2 second Spoon element to compare
	 * @return true if the positions of the elements are the same, false
	 * otherwise
	 */
	private boolean comparePositions(CtElement elem1, CtElement elem2) {
		return Objects.equals(elem1.getPosition().toString(),
			elem2.getPosition().toString());
	}
}
