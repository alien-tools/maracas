package com.github.maracas.validator.cases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.github.maracas.validator.MaracasValidator;
import com.github.maracas.validator.build.MavenArtifactUpgrade;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;
import com.github.maracas.validator.matchers.MissingJApiCompatibilityChange;

import japicmp.model.JApiCompatibilityChange;

/**
 * Maracas validator on the CompChanges project.
 */
public class CompChangesCase {
    /**
     * Main method with the CompChanges pipeline.
     *
     * @param args
     */
    public static void main(String[] args) {
        Path srcApi1 = Paths.get("../test-data/comp-changes/old/");
        Path srcApi2 = Paths.get("../test-data/comp-changes/new/");;
        Path srcClient = Paths.get("../test-data/comp-changes/client/");
        Path report = Paths.get("src/main/resources/comp-changes/report.html");
        MavenArtifactUpgrade upgrade = new MavenArtifactUpgrade(null, null,
            "comp-changes-old", "comp-changes-new", null, null);

        MatcherOptions opts = compChangesOptions();
        MaracasValidator validator = new MaracasValidator(srcApi1, srcApi2, srcClient,
            null, upgrade, opts, true);
        MatcherFilter filter = new CompChangesMatcherFilter(opts);
        validator.validate(filter);

        // Compute metrics
        Map<String, Float> metrics = validator.computeAccuracyMetrics();
        metrics.forEach((k, v) -> System.out.println(String.format("%s: %s", k, v)));

        // Create report
        validator.createHTMLReport(report);
    }

    /**
     * Returns a default instance of the class excluding unsupported breaking
     * changes in Maracas core.
     *
     * @return default matcher options
     */
    private static MatcherOptions compChangesOptions() {
        MatcherOptions opts = new MatcherOptions();

        // Coming from Maracas broken uses (unimplemented)
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_TYPE_CHANGED, "classTypeChanged");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_LESS_ACCESSIBLE, "methodLessAccessible");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_STATIC, "methodNowStatic");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_STATIC, "methodNoLongerStatic");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_STATIC, "methodNowStatic");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_THROWS_CHECKED_EXCEPTION, "methodNowThrosCheckedException");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_THROWS_CHECKED_EXCEPTION, "methodNoLongerThrowsCheckedException");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_TO_CLASS, "methodAbstractAddedToClass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NEW_DEFAULT, "methodNewDefault");
        opts.excludeBreakingChange(JApiCompatibilityChange.CONSTRUCTOR_LESS_ACCESSIBLE, "constructorLessAccessible");

        // Coming from Maracas delta by design
        opts.excludeBreakingChange(JApiCompatibilityChange.SUPERCLASS_MODIFIED_INCOMPATIBLE, "superclassModifiedIncompatible");
        //opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS, "methodRemovedInSuperclass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ADDED_TO_PUBLIC_CLASS, "methodAddedToPublicClass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_SUPERCLASS, "methodAbstractAddedInSuperclass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE, "methodAbstractAddedInImplementedInterface");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE, "methodDefaultAddedInImplementedInterface");
        opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS, "fieldLessAccessibleThanInSuperclass");
        //opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_REMOVED_IN_SUPERCLASS, "fieldRemovedInSuperclass");

        // Missing JApiCmp breaking changes
        opts.excludeBreakingChange(MissingJApiCompatibilityChange.METHOD_MORE_ACCESSIBLE, "methodMoreAccessible");

        return opts;
    }
}
