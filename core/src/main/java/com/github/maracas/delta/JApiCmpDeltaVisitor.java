package com.github.maracas.delta;

import java.util.Iterator;
import java.util.List;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter;
import japicmp.output.Filter.FilterVisitor;

/**
 * japicmp's visitors are awkwardly named and structured.
 * JApiCmpDeltaVisitor is just a facade for japicmp's visitors
 * getting rid of the Iterators<> and "filter" names.
 *
 * @see FilterVisitor
 */
public interface JApiCmpDeltaVisitor {
	public void visit(JApiClass jApiClass);
	public void visit(JApiMethod jApiMethod);
	public void visit(JApiConstructor jApiCons);
	public void visit(JApiImplementedInterface jApiImpl);
	public void visit(JApiField jApiField);
	public void visit(JApiAnnotation jApiAnnotation);
	public void visit(JApiSuperclass jApiSuper);

	public static void visit(List<JApiClass> classes, JApiCmpDeltaVisitor visitor) {
		Filter.filter(classes, new FilterVisitor() {
			@Override
			public void visit(Iterator<JApiClass> iterator, JApiClass jApiClass) {
				visitor.visit(jApiClass);
			}

			@Override
			public void visit(Iterator<JApiMethod> iterator, JApiMethod jApiMethod) {
				visitor.visit(jApiMethod);
			}

			@Override
			public void visit(Iterator<JApiConstructor> iterator, JApiConstructor jApiConstructor) {
				visitor.visit(jApiConstructor);
			}

			@Override
			public void visit(Iterator<JApiImplementedInterface> iterator,
				JApiImplementedInterface jApiImplementedInterface) {
				visitor.visit(jApiImplementedInterface);
			}

			@Override
			public void visit(Iterator<JApiField> iterator, JApiField jApiField) {
				visitor.visit(jApiField);
			}

			@Override
			public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation jApiAnnotation) {
				visitor.visit(jApiAnnotation);
			}

			@Override
			public void visit(JApiSuperclass jApiSuperclass) {
				visitor.visit(jApiSuperclass);
			}
		});
	}
}