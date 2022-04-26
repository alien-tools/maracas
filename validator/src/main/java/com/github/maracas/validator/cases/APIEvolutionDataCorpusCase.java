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

public class APIEvolutionDataCorpusCase {
	public static void main(String[] args) {
		Path srcApi1 = Paths.get("../test-data/api-evolution-data-corpus/lib-v1/");
        Path srcApi2 = Paths.get("../test-data/api-evolution-data-corpus/lib-v2/");;
        Path srcClient = Paths.get("../test-data/api-evolution-data-corpus/client/");
        Path report = Paths.get("src/main/resources/api-evolution-data-corpus/report.html");
        MavenArtifactUpgrade upgrade = new MavenArtifactUpgrade(null, null,
            "api-evolution-data-corpus-lib-v1", "api-evolution-data-corpus-lib-v2", null, null);

        MatcherOptions opts = defaultOptions();
        MaracasValidator validator = new MaracasValidator(srcApi1, srcApi2, srcClient,
            null, upgrade, opts, true);
        MatcherFilter filter = new APIEvolutionDataCorpusMatchFilter(opts);
        validator.validate(filter);

        // Compute metrics
        Map<String, Float> metrics = validator.computeAccuracyMetrics();
        metrics.forEach((k, v) -> System.out.println(String.format("%s: %s", k, v)));

        // Create report
        validator.createHTMLReport(report);
	}

	/**
     * Returns a default instance of the class excluding unsupported breaking
     * changes in Maracas core. It shows the current state of Maracas implementation.
     *
     * @return default matcher options
     */
    public static MatcherOptions defaultOptions() {
    	MatcherOptions opts = new MatcherOptions();

        // Coming from Maracas broken uses (unimplemented)
        //opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_NO_LONGER_PUBLIC, "classNoLongerPublic");
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_TYPE_CHANGED, "classTypeChanged");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_NOW_DEFAULT, "methodAbstractNowDefault");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_LESS_ACCESSIBLE, "methodLessAccessible");
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
