package com.github.maracas.validator.cases;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.github.maracas.validator.MaracasValidator;
import com.github.maracas.validator.build.MavenArtifactUpgrade;

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
        MavenArtifactUpgrade upgrade = new MavenArtifactUpgrade(null, null,
            "comp-changes-old", "comp-changes-new", null, null);

        Map<String, Float> metrics = MaracasValidator.accuracyMetricsFromSrc(srcApi1, srcApi2, srcClient, null, upgrade);
        metrics.forEach((k, v) -> System.out.println(String.format("%s: %s", k, v)));
    }
}
