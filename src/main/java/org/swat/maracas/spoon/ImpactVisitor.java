package org.swat.maracas.spoon;

import java.util.Iterator;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter.FilterVisitor;

public class ImpactVisitor implements FilterVisitor {
	private final ImpactProcessor processor;

	public ImpactVisitor(ImpactProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void visit(Iterator<JApiClass> iterator, JApiClass elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(Iterator<JApiMethod> iterator, JApiMethod elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(Iterator<JApiConstructor> iterator, JApiConstructor elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(Iterator<JApiImplementedInterface> iterator, JApiImplementedInterface elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(Iterator<JApiField> iterator, JApiField elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}

	@Override
	public void visit(JApiSuperclass elem) {
		elem.getCompatibilityChanges().forEach(c -> processor.process(elem, c));
	}
}
