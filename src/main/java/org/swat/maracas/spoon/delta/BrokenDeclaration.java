package org.swat.maracas.spoon.delta;

import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtReference;

public interface BrokenDeclaration {
	public CtReference getReference();
	public JApiCompatibilityChange getChange();
	public BreakingChangeVisitor getVisitor();
}
