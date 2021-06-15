package org.swat.maracas.spoon;

import java.util.ArrayList;
import java.util.List;

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
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

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
		List<CtReference> refs = SpoonHelper.allReferencesToType(model, cls.getFullyQualifiedName());
		
		switch (c) {
			case ANNOTATION_DEPRECATED_ADDED:
			case CLASS_REMOVED:
			case CLASS_LESS_ACCESSIBLE:
			case CLASS_NO_LONGER_PUBLIC:
				
				break;
			case CLASS_NOW_ABSTRACT:
				// New exprs
				break;
			default:
				//throw new UnsupportedOperationException(c.name());
		}
		
		for (CtReference ref : refs) {
			Detection d = new Detection();
			d.setElement(SpoonHelper.firstLocatableParent(ref));
			d.setReference(ref);
			d.setSource(cls.getFullyQualifiedName());
			d.setChange(c);

			if (ref instanceof CtTypeReference) {
				d.setUsedApiElement(((CtTypeReference<?>) ref).getTypeDeclaration());
				d.setUse(APIUse.TYPE_DEPENDENCY);
			} else if (ref instanceof CtExecutableReference) {
				d.setUsedApiElement(((CtExecutableReference<?>) ref).getExecutableDeclaration());
				d.setUse(APIUse.METHOD_INVOCATION);
			} else if (ref instanceof CtFieldReference) {
				d.setUsedApiElement(((CtFieldReference<?>) ref).getFieldDeclaration());
				d.setUse(APIUse.FIELD_ACCESS);
			} else throw new RuntimeException("Unknown ref " + ref);

			detections.add(d);
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
