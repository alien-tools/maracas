package com.github.maracas.validator;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;

import com.github.maracas.validator.build.MavenArtifactUpgrade;
import com.google.common.base.Stopwatch;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@Command(
    name = "Maracas Validator",
    description = "Validate the accuracy of the Maracas tool",
    version = "0.1.0")
public class MaracasValidatorCLI implements Runnable {
    @Option(names = {"-o", "--old"},
        description = "The library's old JAR")
    private Path oldLibJar;

    @Option(names = {"-n", "--new"},
        description = "The library's new JAR")
    private Path newLibJar;

    @Option(names = {"-os", "--oldSrc"},
        description = "Directory containing the library's old source code")
    private Path oldLibSrc;

    @Option(names = {"-ns", "--newSrc"},
        description = "Directory containing the library's new source code")
    private Path newLibSrc;

    @Option(names = {"-c", "--client"}, required = true,
        description = "Directory containing the client's source code")
    private Path clientSrc;

    @Option(names = {"--pom"},
        description = "Relative path to the client's POM file")
    private String clientPOM;

    @Option(names = {"--oldGroupId"},
        description = "The library's old groupID")
    private String oldGroupId;

    @Option(names = {"--newGroupId"},
        description = "The library's new groupID")
    private String newGroupId;

    @Option(names = {"--oldArtifactId"},
        description = "The library's old artifactID")
    private String oldArtifactId;

    @Option(names = {"--newArtifactId"},
        description = "The library's new artifactID")
    private String newArtifactId;

    @Option(names = {"--oldVersion"},
        description = "The library's old version")
    private String oldVersion;

    @Option(names = {"--newVersion"},
        description = "The library's new version")
    private String newVersion;

    @Option(names = {"-r", "--report"},
        description = "The path to the report with the accuracy analysis")
    private Path report;

    @Override
    public void run() {
        try {
            Stopwatch watch = Stopwatch.createStarted();
            MavenArtifactUpgrade mvnValues = new MavenArtifactUpgrade(oldGroupId,
                newGroupId, oldArtifactId, newArtifactId, oldVersion, newVersion);
            MaracasValidator validator;
            Map<String, Float> metrics;

            if (oldLibJar != null && newLibJar != null)
                validator = new MaracasValidator(oldLibJar, newLibJar, clientSrc,
                    clientPOM, mvnValues, null, false);
            else if (oldLibSrc != null && newLibSrc != null)
                validator = new MaracasValidator(oldLibSrc, newLibSrc, clientSrc,
                    clientPOM, mvnValues, null, true);
            else
                throw new RuntimeException("The library's old and new JARs or source code have not been properly defined");

            // Compute metrics
            // TODO: support different MatcherFilters
            validator.validate();
            metrics = validator.computeAccuracyMetrics();

            System.out.println("""
            +----------------+
             ACCURACY METRICS
            +----------------+
            """);

            metrics.forEach((k, v) -> System.out.println(String.format("%s: %s", k, v)));

            // Create HTML report
            if (report != null) {
                validator.createHTMLReport(report);

                System.out.println("""
                    +----------------+
                     HTML REPORT
                    +----------------+
                    """);
                System.out.println(String.format("The report has been written at %s",
                    report.toString()));
            }
            System.out.println(String.format("Done in %s seconds", watch.elapsed(TimeUnit.SECONDS)));
        } catch (Exception e) {
            System.err.println(String.format("Fatal error: %s", e.getMessage()));
        }
    }

    /**
     * Main method of the CLI tool.
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new MaracasValidatorCLI());
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
