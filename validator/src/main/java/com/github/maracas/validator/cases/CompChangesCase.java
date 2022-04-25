package com.github.maracas.validator.cases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.github.maracas.validator.MaracasValidator;
import com.github.maracas.validator.build.MavenArtifactUpgrade;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;

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

        MatcherOptions opts = MatcherOptions.defaultOptions();
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
}
