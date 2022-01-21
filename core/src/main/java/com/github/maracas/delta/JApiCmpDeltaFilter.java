package com.github.maracas.delta;

import com.github.maracas.MaracasOptions;
import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.OutputFilter;

import java.util.Iterator;
import java.util.List;

/**
 * Extends the default OutputFilter's behavior with two additional filters:
 *   - Remove BCs related to anonymous classes, as they're not exposed
 *   - Remove any BCs that is excluded in Maracas' options
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
        if (jApiClass.getFullyQualifiedName().matches(".*\\$[0-9].*"))
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
