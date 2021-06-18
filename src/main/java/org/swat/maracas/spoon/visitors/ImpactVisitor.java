package org.swat.maracas.spoon.visitors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.swat.maracas.spoon.Detection;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter.FilterVisitor;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtTypeReference;

public class ImpactVisitor implements FilterVisitor {
	private final CtPackage root;
	private final Set<Detection> detections = new HashSet<>();

	public ImpactVisitor(CtPackage root) {
		this.root = root;
	}

	public Set<Detection> getDetections() {
		return detections;
	}

	@Override
	public void visit(Iterator<JApiClass> iterator, JApiClass elem) {
		CtTypeReference<?> clsRef = root.getFactory().Type().createReference(elem.getFullyQualifiedName());
		elem.getCompatibilityChanges().forEach(c -> {
			BreakingChangeVisitor visitor = switch (c) {
				case CLASS_LESS_ACCESSIBLE -> new ClassLessAccessibleVisitor(clsRef);
				case CLASS_NOW_ABSTRACT -> new ClassNowAbstractVisitor(clsRef);
				case CLASS_NOW_FINAL -> new ClassNowFinalVisitor(clsRef);
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedVisitor(clsRef);
				default -> {
					System.out.println("Unknown " + c.name());
					yield null;
				}
			};
			
			if (visitor != null) {
				visitor.scan(root);
				detections.addAll(visitor.getDetections());
			}
		});
	}

	@Override
	public void visit(Iterator<JApiMethod> iterator, JApiMethod elem) {
	}

	@Override
	public void visit(Iterator<JApiConstructor> iterator, JApiConstructor elem) {
	}

	@Override
	public void visit(Iterator<JApiImplementedInterface> iterator, JApiImplementedInterface elem) {
	}

	@Override
	public void visit(Iterator<JApiField> iterator, JApiField elem) {
	}

	@Override
	public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation elem) {
	}

	@Override
	public void visit(JApiSuperclass elem) {
	}
}
