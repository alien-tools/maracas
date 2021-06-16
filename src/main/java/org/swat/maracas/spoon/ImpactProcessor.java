package org.swat.maracas.spoon;

import static org.swat.maracas.spoon.SpoonHelper.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.swat.maracas.spoon.Detection.APIUse;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiClassType.ClassType;
import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
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

	@SafeVarargs
	public static <T> List<? extends T> union(List<? extends T>... lists) {
		return Lists.newArrayList(Iterables.concat(lists));
	}

	public void process(JApiClass cls, JApiCompatibilityChange c) {
		CtTypeReference<?> clsRef = toTypeReference(model, cls.getFullyQualifiedName());
		List<? extends CtElement> refs = switch (c) {
			case ANNOTATION_DEPRECATED_ADDED,
				CLASS_REMOVED,
				CLASS_LESS_ACCESSIBLE,
				CLASS_NO_LONGER_PUBLIC ->
				allReferencesToType(model, clsRef);
			case CLASS_NOW_ABSTRACT ->
				allInstantiationsOf(model, clsRef);
			case CLASS_NOW_CHECKED_EXCEPTION ->
				allExpressionsThrowing(model, clsRef);
			case CLASS_NOW_FINAL ->
				allExtensionsOf(model, clsRef);
			case CLASS_TYPE_CHANGED -> { // FIXME: not sure of all the cases here
				ClassType oldType = cls.getClassType().getOldTypeOptional().get();
				if (oldType.equals(ClassType.ANNOTATION)) // Cannot be used as an @annotation anymore
					yield allAnnotationsOfType(model, clsRef);
				if (oldType.equals(ClassType.CLASS)) // Cannot be instantiated nor used as superclass anymore
					yield union(
						allInstantiationsOf(model, clsRef),
						allExtensionsOf(model, clsRef)
					);
				if (oldType.equals(ClassType.INTERFACE)) // Cannot be implemented anymore
					yield allImplementationsOf(model, clsRef);
				if (oldType.equals(ClassType.ENUM)) // FIXME: ...
					yield new ArrayList<>();
				throw new UnsupportedOperationException("Unsupported " + oldType);
			}
				
			case INTERFACE_ADDED,
				METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE,
				METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE,
				FIELD_REMOVED_IN_SUPERCLASS,
				METHOD_ABSTRACT_ADDED_IN_SUPERCLASS,
				METHOD_REMOVED_IN_SUPERCLASS ->
				new ArrayList<>();
			default ->
				throw new UnsupportedOperationException(c.name());
		};
		
		for (CtElement element : refs) {
			Detection d = new Detection();
			d.setElement(firstLocatableParent(element));
			//d.setReference(element);
			d.setSource(clsRef);
			d.setChange(c);
			
			if (element instanceof CtTypeReference) {
				d.setUsedApiElement(((CtTypeReference<?>) element).getTypeDeclaration());
				d.setUse(APIUse.TYPE_DEPENDENCY);
				
				if (element.getParent() instanceof CtType) {
					CtType<?> parent = (CtType<?>) element.getParent();
					if (element.equals(parent.getSuperclass()))
						d.setUse(APIUse.EXTENDS);
					if (parent.getSuperInterfaces().contains(element))
						d.setUse(APIUse.IMPLEMENTS);
				}
			} else if (element instanceof CtExecutableReference) {
				d.setUsedApiElement(((CtExecutableReference<?>) element).getExecutableDeclaration());
				d.setUse(APIUse.METHOD_INVOCATION);
			} else if (element instanceof CtFieldReference) {
				d.setUsedApiElement(((CtFieldReference<?>) element).getFieldDeclaration());
				d.setUse(APIUse.FIELD_ACCESS);
			} else if (element instanceof CtThrow) {
				d.setUsedApiElement(((CtThrow) element).getThrownExpression().getType());
				d.setUse(APIUse.TYPE_DEPENDENCY);
			} else if (element instanceof CtClass) {
				d.setUsedApiElement(clsRef); // FIXME: ...
				d.setUse(APIUse.EXTENDS);
			} else if (element instanceof CtInterface) {
				d.setUsedApiElement(clsRef); // FIXME: ...
				d.setUse(APIUse.IMPLEMENTS);
			} else if (element instanceof CtConstructorCall) {
				d.setUsedApiElement(((CtConstructorCall<?>) element).getType());
				d.setUse(APIUse.METHOD_INVOCATION);
			} else if (element instanceof CtAnnotation) {
				d.setUsedApiElement(((CtAnnotation<?>) element).getType());
				d.setUse(APIUse.ANNOTATION);
			} else {
				throw new RuntimeException("Unknown element " + element.getClass());
			}

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
