package com.github.maracas.validator.cases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

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
        MatcherFilter filter = new CasesDefaultMatcherFilter(opts);
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
     * Using {@code null} as value if there are no specific cases reported in
     * the corpus for the given breaking change.
     *
     * @return default matcher options
     */
    public static MatcherOptions defaultOptions() {
    	MatcherOptions opts = new MatcherOptions();

        // Coming from Maracas broken uses (unimplemented)
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_NO_LONGER_PUBLIC);
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_TYPE_CHANGED);
        opts.excludeBreakingChange(JApiCompatibilityChange.CONSTRUCTOR_LESS_ACCESSIBLE);
        opts.excludeCompilerMessage(".+accessModifierClazzConstructorAccessDecrease.+");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_NOW_DEFAULT);
        opts.excludeCompilerMessage(".+membersIfazeMethodDefaultAdd.+");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_LESS_ACCESSIBLE);
        opts.excludeCompilerMessage(".+accessModifierClazzMethodAccessDecrease.+");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_STATIC);
        opts.excludeCompilerMessage(".+modifierMethodStaticToNonStatic.+");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_STATIC);
        opts.excludeCompilerMessage(".+modifierMethodNonStaticToStatic.+");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_THROWS_CHECKED_EXCEPTION);
        opts.excludeCompilerMessage(Set.of(".+exceptionClazzMethodThrowCheckedAdd.+", ".+exceptionClazzMethodTryCatchToThrowChecked.+"));
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_THROWS_CHECKED_EXCEPTION);
        opts.excludeCompilerMessage(Set.of(".+exceptionClazzMethodThrowCheckedDelete.+", ".+exceptionClazzMethodThrowCheckedToTryCatch.+"));
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_TO_CLASS);
        opts.excludeCompilerMessage(Set.of(".+membersClazzMethodAbstractAdd.+", "membersIfazeMethodAdd.+"));
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NEW_DEFAULT);
        opts.excludeCompilerMessage(".+membersIfazeMethodDefaultAdd.+");

        // Coming from Maracas delta by design
        opts.excludeBreakingChange(JApiCompatibilityChange.SUPERCLASS_MODIFIED_INCOMPATIBLE);
        //opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS);
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ADDED_TO_PUBLIC_CLASS);
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_SUPERCLASS);
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE);
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE);
        opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS);
        //opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_REMOVED_IN_SUPERCLASS);

        // Missing JApiCmp breaking changes
        opts.excludeBreakingChange(MissingJApiCompatibilityChange.METHOD_MORE_ACCESSIBLE);
        opts.excludeCompilerMessage(Set.of(".+accessModifierClazzMethodAccessIncrease.+", ".+accessModifierClazzConstructorAccessIncreasee.+"));
        opts.excludeCompilerMessage(".+generics.+");

        return opts;
    }
}
