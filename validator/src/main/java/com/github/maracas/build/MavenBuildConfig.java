package com.github.maracas.build;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maven build configuration
 */
public record MavenBuildConfig(
    String srcDir,
    String pom,
    List<String> goals,
    List<String> properties) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MavenBuildHandler.class);

    /**
     * Creates a MavenBuildConfig instance using the default settings.
     *
     * @param srcDir path to the Maven project
     */
    public MavenBuildConfig(String srcDir) {
        this(srcDir, null, null, null);
    }

    /**
     * Creates a MavenBuildConfig instance using custom settings.
     *
     * @param srcDir     path to the Maven project
     * @param pom        path to POM file of the project
     * @param goals      Maven execution goals (e.g. compile)
     * @param properties Maven execution properties (e.g. skipTests)
     */
    public MavenBuildConfig(String srcDir, String pom, List<String> goals, List<String> properties) {
        assert srcDir != null;
        this.srcDir = srcDir;
        this.pom = pom != null
            ? pom
            : "pom.xml";
        this.goals = goals != null && !goals.isEmpty()
            ? goals
            : List.of("compile");
        this.properties = properties != null && !properties.isEmpty()
            ? properties
            : List.of("skipTests");
    }
}
