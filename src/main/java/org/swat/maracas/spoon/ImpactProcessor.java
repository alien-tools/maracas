package org.swat.maracas.spoon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class ImpactProcessor {
	private final CtModel model;
	private final Set<Detection> detections = new HashSet<>();

	public ImpactProcessor(CtModel model) {
		this.model = model;
	}

	public Set<Detection> getDetections() {
		return detections;
	}

	public void process(JApiClass cls, JApiCompatibilityChange c) {
		
		List<CtReference> refs = switch (c) {
			case ANNOTATION_DEPRECATED_ADDED,
				CLASS_REMOVED,
				CLASS_LESS_ACCESSIBLE,
				CLASS_NO_LONGER_PUBLIC ->
				SpoonHelper.allReferencesToType(model, cls.getFullyQualifiedName());
			case CLASS_NOW_ABSTRACT ->
				SpoonHelper.allReferencesToType(model, cls.getFullyQualifiedName(),
					ref -> ref instanceof CtExecutableReference && ((CtExecutableReference<?>) ref).isConstructor());
			case CLASS_TYPE_CHANGED,
				CLASS_NOW_CHECKED_EXCEPTION,
				CLASS_NOW_FINAL,
				INTERFACE_ADDED,
				METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE,
				FIELD_REMOVED_IN_SUPERCLASS,
				METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE,
				METHOD_ABSTRACT_ADDED_IN_SUPERCLASS,
				METHOD_REMOVED_IN_SUPERCLASS ->
				new ArrayList<>();
			default ->
				throw new UnsupportedOperationException(c.name());
		};
		
		for (CtReference ref : refs) {
			Detection d = new Detection();
			d.setElement(SpoonHelper.firstLocatableParent(ref));
			d.setReference(ref);
			d.setSource(cls.getFullyQualifiedName());
			d.setChange(c);
			
			if (ref instanceof CtTypeReference) {
				d.setUsedApiElement(((CtTypeReference<?>) ref).getTypeDeclaration());
				d.setUse(APIUse.TYPE_DEPENDENCY);
				
				if (ref.getParent() instanceof CtType) {
					CtType parent = (CtType) ref.getParent();
					if (ref.equals(parent.getSuperclass()))
						d.setUse(APIUse.EXTENDS);
					if (parent.getSuperInterfaces().contains(ref))
						d.setUse(APIUse.IMPLEMENTS);
				}
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
