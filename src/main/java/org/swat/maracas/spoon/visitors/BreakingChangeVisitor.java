package org.swat.maracas.spoon.visitors;

import java.util.HashSet;
import java.util.Set;

import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.delta.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtAbstractVisitor;

public abstract class BreakingChangeVisitor extends CtAbstractVisitor {
	private final JApiCompatibilityChange change;
	private final Set<Detection> detections = new HashSet<>();

	protected BreakingChangeVisitor(JApiCompatibilityChange change) {
		super();
		this.change = change;
	}

	protected void detection(CtElement element, CtElement usedApiElement, CtReference source, APIUse use) {
		Detection d = new Detection(
			element,
			usedApiElement,
			source,
			use,
			change
		);

		detections.add(d);
	}

	public Set<Detection> getDetections() {
		return detections;
	}

	//
	// Common utilities
	//
	protected String getEnclosingPkgName(CtElement e) {
		CtPackage enclosing = e.getParent(CtPackage.class);
		return
			enclosing != null ?
				enclosing.getQualifiedName() :
				"";
	}
}
