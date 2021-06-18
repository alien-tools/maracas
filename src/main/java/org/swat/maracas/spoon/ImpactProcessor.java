package org.swat.maracas.spoon;

import static org.swat.maracas.spoon.SpoonHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import javassist.CtMethod;
import spoon.reflect.CtModel;
import spoon.reflect.reference.CtExecutableReference;
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

	@SafeVarargs
	public static <T> List<T> union(List<T>... lists) {
		return Lists.newArrayList(Iterables.concat(lists));
	}

	// FIXME: to debug later, insert all detections as comments
	// and output comp-changes to check manually
	public void process(JApiClass cls, JApiCompatibilityChange c) {
		CtTypeReference<?> clsRef = toTypeReference(model, cls.getFullyQualifiedName());
		List<Detection> ds = switch (c) {
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
				union(
					allClassesExtending(model, clsRef),
					allAnonymousClassesOf(model, clsRef)
				);
			case CLASS_TYPE_CHANGED -> { // FIXME: not sure of all the cases here
				ClassType oldType = cls.getClassType().getOldTypeOptional().get();
				if (oldType.equals(ClassType.ANNOTATION)) // Cannot be used as an @annotation anymore
					yield allAnnotationsOfType(model, clsRef);
				if (oldType.equals(ClassType.CLASS)) // Cannot be instantiated nor used as superclass anymore
					yield union(
						allInstantiationsOf(model, clsRef),
						allClassesExtending(model, clsRef)
					);
				if (oldType.equals(ClassType.INTERFACE)) // Cannot be implemented anymore
					yield allClassesImplementing(model, clsRef);
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
		
		for (Detection d : ds) {
			d.setSource(clsRef);
			d.setChange(c);
			
			detections.add(d);
		}
	}

	public void process(JApiMethod m, JApiCompatibilityChange c) {
		JApiClass cls = m.getjApiClass();
		CtTypeReference<?> clsRef = toTypeReference(model, cls.getFullyQualifiedName());
		if (m.getOldMethod().isPresent()) {
			CtMethod oldMethod = m.getOldMethod().get();
			Optional<CtExecutableReference<?>> mthRefOpt = clsRef.getAllExecutables()
				.stream()
				.filter(mth -> mth.getSimpleName().equals(oldMethod.getLongName()) || mth.getSimpleName().equals(oldMethod.getName()))
				.findAny();

			if (!mthRefOpt.isPresent()) {
				System.out.println("Skipping unknown " + m.getOldMethod().get().getLongName());
				System.out.println("\t"+clsRef.getAllExecutables());
			}
			
				CtExecutableReference<?> mthRef = mthRefOpt.get();
				List<Detection> ds = switch (c) {
					case ANNOTATION_DEPRECATED_ADDED ->
						allReferencesToMethod(model, mthRef);
					case METHOD_REMOVED ->
						// TODO: Is japicmp making the right choice?
						// a method that no longer throws a checked exception is counted as METHOD_REMOVED
						// which isn't an issue at the source level (it probably is at the binary level)
						allReferencesToMethod(model, mthRef);
					default ->
						new ArrayList<>();
						//throw new UnsupportedOperationException(c.name());
				};

				for (Detection d : ds) {
					d.setSource(mthRef);
					d.setChange(c);

					detections.add(d);
				}
		} else {
			return;
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
