package com.github.maracas.build;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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
            : List.of("clean", "compile");
        this.properties = properties != null && !properties.isEmpty()
            ? properties
            : List.of("skipTests");
    }

    /**
     * Transforms the list of properties (String) of the {@link MavenBuildConfig}
     * instance into a {@link Properties} object. In particular, it sets each
     * property in the list to true.
     *
     * @return Maven properties
     */
    public static Properties getMavenProperties(MavenBuildConfig config) {
        Properties properties = new Properties();
        config.properties().forEach(p -> properties.put(p, "true"));
        return properties;
    }

    /**
     * Validate an instance of the MavenBuildConfig class--that is, it checks
     * that both the source directory and the POM file exist in the file system.
     * If not, the method throws a {@link BuildException}.
     * ODO: Maybe the BuildException is not the right one here.
     *
     * @param config instance of the MavenBuildConfig class.
     */
    public static void validate(MavenBuildConfig config) {
        File srcDir = new File(config.srcDir());
        if (!srcDir.exists())
           throw new BuildException("The source directory cannot be found");

        File pom = srcDir.toPath().resolve(Paths.get(config.pom())).toFile();
        if (!pom.exists())
            throw new BuildException("The POM file of the projcet cannot be found");
    }
}
