package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtReference;

/**
 * A broken declaration is a declaration in the library's code impacted
 * by a breaking change.
 */
public interface BrokenDeclaration {
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
	 * impact this broken declaration has on client code
	 */
	BreakingChangeVisitor getVisitor();

	/**
	 * Returns the {@link CtElement} in the library's source code corresponding
	 * to the broken declaration's {@link #getReference()}, if any.
	 */
	CtElement getSourceElement();

	/**
	 * Sets the the {@link CtElement} in the library's source code associated with
	 * the declaration's {@link #getReference()}
	 */
	void setSourceElement(CtElement element);
}
