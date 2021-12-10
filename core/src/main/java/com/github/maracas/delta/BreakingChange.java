package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtReference;

/**
 * A breaking change is a change in a library's declaration that potentially
 * breaks client code.
 */
public interface BreakingChange {
	/**
	 * Returns a {@link CtReference} pointer to the impacted declaration
	 */
	CtReference getReference();

	/**
	 * Returns a the kind of breaking change, as specified in {@link JApiCompatibilityChange}
	 */
	JApiCompatibilityChange getChange();

	/**
	 * Returns the {@link BreakingChangeVisitor} responsible for detecting the
	 * impact this breaking change has on client code
	 */
	BreakingChangeVisitor getVisitor();

	/**
	 * Returns the {@link CtElement} in the library's source code corresponding
	 * to the breaking change's {@link #getReference()}, if any.
	 */
	CtElement getSourceElement();

	/**
	 * Sets the the {@link CtElement} in the library's source code associated with
	 * the declaration's {@link #getReference()}
	 */
	void setSourceElement(CtElement element);
}
