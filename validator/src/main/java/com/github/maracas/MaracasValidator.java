package com.github.maracas;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.github.maracas.accuracy.AccuracyAnalyzer;
import com.github.maracas.accuracy.AccuracyCase;
import com.github.maracas.accuracy.LocationMatcher;
import com.github.maracas.accuracy.Matcher;
import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.build.BuildHandler;
import com.github.maracas.build.CompilerMessage;
import com.github.maracas.build.MavenBuildConfig;
import com.github.maracas.build.MavenBuildHandler;
import com.github.maracas.delta.Delta;

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
     * @param srcApi1   absolute path to the source project of the old library release
     * @param srcApi2   absolute path to the source project of the new library release
     * @param srcClient absolute path to the source project of the client
     * @return map with accuracy metrics
     */
    public static Map<String,Float> accuracyMetricsFromSrc(String srcApi1, String srcApi2, String srcClient) {
        Path api1 = Paths.get(srcApi1);
        Path api2 = Paths.get(srcApi2);

        // Generate JAR in target folder
        logger.info("Packing libraries: {} and {}", srcApi1, srcApi2);
        packageMavenProject(api1);
        packageMavenProject(api2);

        // Get path of the previously generated JARs
        String jarApi1 = getJarPath(api1).toString();
        String jarApi2 = getJarPath(api2).toString();

        return accuracyMetricsFromJars(jarApi1, jarApi2, srcClient);
    }

    /**
     * Returns a map with the accuracy metrics (e.g., precision, recall) of
     * Maracas. Keys are the names of the metrics, and values are float numbers
     * representing the corresponding value. The metrics are computed based on
     * input JARs of the library and the source project of the client.
     *
     * @param jarApi1   absolute path to the JAR of the old library release
     * @param jarApi2   absolute path to the JAR of the new library release
     * @param srcClient absolute path to the source project of the client
     * @return map with accuracy metrics
     */
    public static Map<String,Float> accuracyMetricsFromJars(String jarApi1, String jarApi2, String srcClient) {
        Path jar1 = Paths.get(jarApi1);
        Path jar2 = Paths.get(jarApi2);
        Path client = Paths.get(srcClient);

        // Compute Maracas data
        logger.info("Computing delta and broken uses for client {}", srcClient);
        BuildHandler handler = new MavenBuildHandler(client);
        Delta delta = Maracas.computeDelta(jar1, jar2);
        Collection<BrokenUse> brokenUses = Maracas.computeBrokenUses(client, delta);
        logger.info("Compiling client source code");
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

    /**
     * Returns the JAR path of a Maven project after packing it in the target
     * folder.
     *
     * @param src absolute path to the source project
     * @return path to the JAR file
     */
    private static Path getJarPath(Path src) {
        return getJarPath(src, null);
    }

    /**
     * Returns the JAR path of a Maven project after packing it in the target
     * folder.
     *
     * @param src             absolute path to the source project
     * @param relativePOMPath relative path to the POM file in the source project
     * @return path to the JAR file
     */
    private static Path getJarPath(Path src, String relativePOMPath) {
        if (relativePOMPath == null)
            relativePOMPath = "pom.xml";

        Path pom = src.resolve(relativePOMPath);
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;

        try {
            model = pomReader.read(new FileInputStream(pom.toFile()));
            String artifactId = model.getArtifactId();
            String version = model.getVersion();
            Path targetDir = src.resolve("target");
            Path jar = targetDir.resolve(String.format("%s-%s.jar", artifactId, version));
            return jar;
        } catch (IOException | XmlPullParserException e) {
            logger.error(e);
        }
        return null;
    }
}
