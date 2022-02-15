package com.github.maracas.validator;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.Maracas;
import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.delta.Delta;
import com.github.maracas.validator.accuracy.AccuracyAnalyzer;
import com.github.maracas.validator.accuracy.AccuracyCase;
import com.github.maracas.validator.accuracy.LocationMatcher;
import com.github.maracas.validator.accuracy.Matcher;
import com.github.maracas.validator.build.BuildHandler;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.build.MavenArtifactUpgrade;
import com.github.maracas.validator.build.MavenBuildConfig;
import com.github.maracas.validator.build.MavenBuildHandler;
import com.github.maracas.validator.build.MavenHelper;

/**
 * Maracas validator API
 */
public class MaracasValidator {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MaracasValidator.class);

    /**
     * Constructor of the class. It does not allow the instantiation of the
     * class. Only use static methods.
     */
    private MaracasValidator() {}

    /**
     * Returns a map with the accuracy metrics (e.g., precision, recall) of
     * Maracas. Keys are the names of the metrics, and values are float numbers
     * representing the corresponding value. The metrics are computed based on
     * input source projects.
     *
     * @param srcApi1   path to the source project of the old library release
     * @param srcApi2   path to the source project of the new library release
     * @param srcClient path to the source project of the client
     * @param clientPOM relative path to the client POM file
     * @param upgrade   Maven artifact upgrade values
     * @return map with accuracy metrics
     */
    public static Map<String,Float> accuracyMetricsFromSrc(Path srcApi1, Path srcApi2,
        Path srcClient, String clientPOM, MavenArtifactUpgrade upgrade) {
        // Generate JAR in target folder
        logger.info("Packing libraries: {} and {}", srcApi1, srcApi2);
        packageMavenProject(srcApi1);
        packageMavenProject(srcApi2);

        // Get path of the previously generated JARs
        Path jarApi1 = MavenHelper.getJarPath(srcApi1);
        Path jarApi2 = MavenHelper.getJarPath(srcApi2);

        return accuracyMetricsFromJars(jarApi1, jarApi2, srcClient, clientPOM, upgrade);
    }

    /**
     * Returns a map with the accuracy metrics (e.g., precision, recall) of
     * Maracas. Keys are the names of the metrics, and values are float numbers
     * representing the corresponding value. The metrics are computed based on
     * input JARs of the library and the source project of the client.
     *
     * @param jarApi1   path to the JAR of the old library release
     * @param jarApi2   path to the JAR of the new library release
     * @param srcClient path to the source project of the client
     * @param clientPOM relative path to the client POM file
     * @param upgrade   Maven artifact upgrade values
     * @return map with accuracy metrics
     */
    public static Map<String,Float> accuracyMetricsFromJars(Path jarApi1, Path jarApi2,
        Path srcClient, String clientPOM, MavenArtifactUpgrade upgrade) {
        // Compute Maracas data
        logger.info("Computing delta and broken uses for client {}", srcClient);
        BuildHandler handler = new MavenBuildHandler(srcClient);
        Delta delta = Maracas.computeDelta(jarApi1, jarApi2);
        Collection<BrokenUse> brokenUses = Maracas.computeBrokenUses(srcClient, delta);

        logger.info("Updating and compiling client source code");
        MavenHelper.updateDependency(srcClient, clientPOM, upgrade);
        List<CompilerMessage> messages = handler.gatherCompilerMessages();

        // Match cases and analyze
        logger.info("Matching compiler messages against broken uses");
        Matcher matcher = new LocationMatcher();
        Collection<AccuracyCase> cases = matcher.match(brokenUses, messages);
        AccuracyAnalyzer analyzer = new AccuracyAnalyzer(cases);

        // Gather accuracy metrics
        logger.info("Compuitng accuracy metrics");
        Map<String,Float> metrics = new HashMap<String,Float>();
        metrics.put("precision", analyzer.precision());
        metrics.put("recall", analyzer.recall());
        metrics.put("true-positives", (float) analyzer.truePositives().size());
        metrics.put("false-positives", (float) analyzer.falsePositives().size());
        metrics.put("false-negatives", (float) analyzer.falseNegatives().size());
        return metrics;
    }

    /**
     * Cleans and packages a Maven source project.
     *
     * @param src source project path
     */
    private static void packageMavenProject(Path src) {
        MavenBuildConfig config = new MavenBuildConfig(src.toString(),
            null, List.of("clean", "package"), null);
        BuildHandler handler = new MavenBuildHandler(src, config);
        handler.build();
    }
}
