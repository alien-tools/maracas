package org.swat.maracas.spoon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.swat.maracas.spoon.Detection.APIUse;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import spoon.reflect.CtModel;

public class ImpactProcessor {
	private final CtModel model;
	private final List<Detection> detections = new ArrayList<>();

	public ImpactProcessor(CtModel model) {
		this.model = model;
	}

	public List<Detection> getDetections() {
		return detections;
	}

	public void process(JApiClass cls, JApiCompatibilityChange c) {
		switch (c) {
			case ANNOTATION_DEPRECATED_ADDED:
			case CLASS_LESS_ACCESSIBLE:
			case CLASS_NO_LONGER_PUBLIC:
			case CLASS_REMOVED:
				detections.addAll(
					SpoonHelper.allReferencesToType(model, cls.getFullyQualifiedName())
						.stream()
						.map(d -> {
							d.setSource(cls.getFullyQualifiedName());
							d.setChange(c);
							return d;
						})
						.collect(Collectors.toList()));
				break;
			default:
				//throw new UnsupportedOperationException(c.name());
		}
	}

	public Detection process(JApiMethod m, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}

	public Detection process(JApiConstructor cons, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}

	public Detection process(JApiImplementedInterface intf, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}

	public Detection process(JApiField f, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}

	public Detection process(JApiAnnotation ann, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}

	public Detection process(JApiSuperclass superCls, JApiCompatibilityChange c) {
		switch (c) {
			default:
				//throw new UnsupportedOperationException(c.name());
				return null;
		}
	}
}
