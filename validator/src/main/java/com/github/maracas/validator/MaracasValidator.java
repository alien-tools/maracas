package com.github.maracas.validator;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.maracas.SourcesDirectory;
import com.github.maracas.LibraryJar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.Maracas;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.validator.accuracy.AccuracyAnalyzer;
import com.github.maracas.validator.accuracy.AccuracyCase;
import com.github.maracas.validator.build.BuildHandler;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.build.MavenArtifactUpgrade;
import com.github.maracas.validator.build.MavenBuildConfig;
import com.github.maracas.validator.build.MavenBuildHandler;
import com.github.maracas.validator.build.MavenHelper;
import com.github.maracas.validator.matchers.Matcher;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;
import com.github.maracas.validator.matchers.MavenLocationMatcher;
import com.github.maracas.validator.viz.HTMLReportVisualizer;
import com.github.maracas.validator.viz.ReportVisualizer;

/**
 * Maracas validator API
 */
public class MaracasValidator {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MaracasValidator.class);

    private Path jarApi1;
    private Path jarApi2;
    private Path srcClient;
    private String pomClient;
    private MavenArtifactUpgrade upgrade;
    private MatcherOptions opts;
    private Set<BrokenUse> brokenUses;
    private Set<CompilerMessage> messages;
    private Collection<AccuracyCase> cases;

    /**
     * Creates a MaracasValidator instance.
     *
     * @param api1      path to the source code or JAR file of the old library
     *                  release
     * @param api2      path to the source code or JAR file of the new library
     *                  release
     * @param srcClient path to the source project of the client
     * @param pomClient relative path to the client POM file
     * @param upgrade   Maven artifact upgrade values
     * @param opts      {@link MatcherOptions} instance to exclude a subset of
     *                  breaking changes
     * @param sources   true if api1 and api2 point to source code, false if
     *                  they point to JAR files
     */
    public MaracasValidator(Path api1, Path api2, Path srcClient, String pomClient,
        MavenArtifactUpgrade upgrade, MatcherOptions opts, boolean sources) {
        this.srcClient = srcClient;
        this.pomClient = pomClient;
        this.upgrade = upgrade;
        this.opts = opts;

        if (sources) {
            // Generate JAR in target folder
            logger.info("Packing libraries: {} and {}", api1, api2);
            packageMavenProject(api1);
            packageMavenProject(api2);

            // Get path of the previously generated JARs
            this.jarApi1 = MavenHelper.getJarPath(api1);
            this.jarApi2 = MavenHelper.getJarPath(api2);
        } else {
            this.jarApi1 = api1;
            this.jarApi2 = api2;
        }
    }

    /**
     * Computes the set of broken uses, compiler messages, and accuracy cases.
     */
    public void validate() {
        validate(null);
    }

    /**
     * Computes the set of broken uses, compiler messages, and accuracy cases.
     *
     * @param filter {@link MatcherFilter} instance to filter unneeded broken
     *               uses and compiler messages
     */
    public void validate(MatcherFilter filter) {
        // Compute Maracas data
        logger.info("Computing delta and broken uses for client {}", srcClient);
        BuildHandler handler = new MavenBuildHandler(srcClient);
        LibraryJar v1 = LibraryJar.withoutSources(jarApi1);
        LibraryJar v2 = LibraryJar.withoutSources(jarApi2);
        SourcesDirectory client = SourcesDirectory.of(srcClient);
        Delta delta = Maracas.computeDelta(v1, v2);
        DeltaImpact deltaImpact = Maracas.computeDeltaImpact(client, delta);
        this.brokenUses = deltaImpact.brokenUses();

        logger.info("Updating and compiling client source code");
        MavenHelper.updateDependency(srcClient, pomClient, upgrade);
        MavenHelper.configureMavenCompiler(srcClient, pomClient, 10000);
        this.messages = handler.gatherCompilerMessages();

        // Filter broken uses and compiler messages if needed
        if (filter != null) {
            logger.info("Filtering broken uses and compiler messages");
            filterCases(filter);
        }

        // Match cases and analyze
        logger.info("Matching compiler messages against broken uses");
        Matcher matcher = new MavenLocationMatcher();
        this.cases = matcher.match(brokenUses, messages);
    }

    /**
     * Filters out broken uses and compiler messages based on the {@link MatcherFilter}
     * instance.
     *
     * @param filter {@link MatcherFilter} instance to filter unneeded broken
     *               uses and compiler messages
     */
    private void filterCases(MatcherFilter filter) {
        Set<BrokenUse> filteredBrokenUses = filter.filterBrokenUses(brokenUses);
        Set<CompilerMessage> filteredMessages = filter.filterCompilerMessages(messages);
        this.brokenUses = filteredBrokenUses;
        this.messages = filteredMessages;
    }

    /**
     * Returns a map with the accuracy metrics (e.g., precision, recall) of
     * Maracas. Keys are the names of the metrics, and values are float numbers
     * representing the corresponding value. The {@link #validate()} method
     * must be called first.
     *
     * @return map with accuracy metrics
     */
    public Map<String,Float> computeAccuracyMetrics() {
        assert cases != null : "The tool has not been validated yet: accuracy cases are null";
        AccuracyAnalyzer analyzer = new AccuracyAnalyzer(cases);

        // Gather accuracy metrics
        logger.info("Computing accuracy metrics");
        Map<String,Float> metrics = new HashMap<String,Float>();
        metrics.put("precision", analyzer.precision());
        metrics.put("recall", analyzer.recall());
        metrics.put("true-positives", (float) analyzer.truePositives().size());
        metrics.put("false-positives", (float) analyzer.falsePositives().size());
        metrics.put("false-negatives", (float) analyzer.falseNegatives().size());
        return metrics;
    }

    /**
     * Creates an HTML report of the computed accuracy cases. The {@link #validate()}
     * method must be called first and report must not be null.
     *
     * @param report path to the report file with the accuracy cases data
     */
    public void createHTMLReport(Path report) {
        assert cases != null : "The tool has not been validated yet: accuracy cases are null";
        assert report != null : "The report path is null";

        logger.info("Creating report at {}", report);
        ReportVisualizer viz = new HTMLReportVisualizer(cases, report);
        viz.generate();
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
