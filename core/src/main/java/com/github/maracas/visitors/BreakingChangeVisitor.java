package com.github.maracas.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.detection.APIUse;
import com.github.maracas.detection.Detection;
import com.github.maracas.util.SpoonHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
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
		// We may encounter detections on synthetic/implicit code elements
		// (e.g., default constructors, super() calls, etc.) that do not have
		// position information since they're absent from source code.
		// => just point the first locatable parent element
		CtElement locatableElement =
			element.getPosition() instanceof NoSourcePosition ?
				SpoonHelpers.firstLocatableParent(element) :
				element;

		Detection d = new Detection(
			locatableElement,
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
