package org.swat.maracas.spoon.visitors;

import java.util.HashSet;
import java.util.Set;

import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.Detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;

public abstract class BreakingChangeVisitor extends CtScanner {
	private final JApiCompatibilityChange change;
	private final Set<Detection> detections = new HashSet<>();

	protected BreakingChangeVisitor(JApiCompatibilityChange change) {
		super();
		this.change = change;
	}

	protected void detection(CtElement element, CtElement usedApiElement, CtReference source, APIUse use) {
		Detection d = new Detection();
		d.setElement(element);
		d.setUsedApiElement(usedApiElement);
		d.setSource(source);
		d.setUse(use);
		d.setChange(change);
		detections.add(d);
	}

	public Set<Detection> getDetections() {
		return detections;
	}
}
