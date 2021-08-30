package org.swat.maracas.spoon.delta;

import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;

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
