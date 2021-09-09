package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtReference;

public interface BrokenDeclaration {
	public CtReference getReference();
	public JApiCompatibilityChange getChange();
	public BreakingChangeVisitor getVisitor();
	public CtElement getSourceElement();
	public void setSourceElement(CtElement element);
}
