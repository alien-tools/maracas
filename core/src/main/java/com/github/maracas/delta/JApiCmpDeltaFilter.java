package com.github.maracas.delta;

import com.github.maracas.MaracasOptions;
import japicmp.model.*;
import japicmp.output.OutputFilter;

import java.util.Iterator;
import java.util.List;

/**
 * Extends the default {@link OutputFilter}'s behavior with additional filters:
 * - Remove BCs related to anonymous classes, as they're not exposed
 * - Remove BCs on NEW types, as they won't affect anyone
 * - Remove any BCs that is excluded in Maracas' options
 */
public class JApiCmpDeltaFilter extends OutputFilter {
	private final MaracasOptions maracasOptions;

	public JApiCmpDeltaFilter(MaracasOptions options) {
		super(options.getJApiOptions());
		this.maracasOptions = options;
	}

	@Override
	public void filter(List<JApiClass> jApiClasses) {
		filter(jApiClasses, new FilterVisitor() {
			@Override
			public void visit(Iterator<JApiClass> iterator, JApiClass jApiClass) {
				if (
					jApiClass.getFullyQualifiedName().matches(".*\\$\\d.*")
					|| jApiClass.getChangeStatus().equals(JApiChangeStatus.NEW)
				)
					iterator.remove();

				jApiClass.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(Iterator<JApiMethod> iterator, JApiMethod jApiMethod) {
				jApiMethod.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(Iterator<JApiConstructor> iterator, JApiConstructor jApiConstructor) {
				jApiConstructor.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(Iterator<JApiImplementedInterface> iterator, JApiImplementedInterface jApiImplementedInterface) {
				jApiImplementedInterface.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(Iterator<JApiField> iterator, JApiField jApiField) {
				jApiField.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation jApiAnnotation) {
				jApiAnnotation.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}

			@Override
			public void visit(JApiSuperclass jApiSuperclass) {
				jApiSuperclass.getCompatibilityChanges().removeAll(maracasOptions.getExcludedBreakingChanges());
			}
		});

		super.filter(jApiClasses);
	}
}
