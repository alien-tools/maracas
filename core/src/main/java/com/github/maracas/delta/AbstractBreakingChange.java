package com.github.maracas.delta;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;

import java.util.Objects;

public abstract class AbstractBreakingChange implements BreakingChange {
	protected final JApiCompatibilityChange change;
	protected CtElement sourceElement;

	protected AbstractBreakingChange(JApiCompatibilityChange change) {
		this.change = Objects.requireNonNull(change);
	}

	@Override
	public JApiCompatibilityChange getChange() {
		return change;
	}

	@Override
	public CtElement getSourceElement() {
		return this.sourceElement;
	}

	@Override
	public void setSourceElement(CtElement element) {
		this.sourceElement = Objects.requireNonNull(element);
	}
}
