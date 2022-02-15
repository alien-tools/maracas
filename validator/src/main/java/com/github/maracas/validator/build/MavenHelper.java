package com.github.maracas.validator.build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Class with Maven helper methods
 */
public class MavenHelper {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MavenHelper.class);

    /**
     * Constructor of the class. It does not allow the instantiation of the
     * class. Only use static methods.
     */
    private MavenHelper() { }

    /**
     * Returns the JAR path of a Maven project after packing it in the target
     * folder.
     *
     * @param src absolute path to the source project
     * @return path to the JAR file
     */
    public static Path getJarPath(Path src) {
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
    public static Path getJarPath(Path src, String relativePOMPath) {
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
            logger.error("Couldn't resolve JAR path", e);
        }
        return null;
    }

    /**
     * Updates the values of a dependency in the POM file.
     *
     * @param src             absolute path to the source project
     * @param relativePOMPath relative path to the POM file in the source project
     * @param upgrade         {@link MavenArtifactUpgrade} with the artifact
     *                        upgrade information
     */
    public static void updateDependency(Path src, String relativePOMPath, MavenArtifactUpgrade upgrade) {
        if (relativePOMPath == null)
            relativePOMPath = "pom.xml";

        Path pom = src.resolve(relativePOMPath);
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;

        try {
            model = pomReader.read(new FileInputStream(pom.toFile()));
            logger.info("Patching dependency in POM file {}", upgrade);

            for (Dependency dependency : model.getDependencies()) {
                if (upgrade.oldArtifactId() != null
                    && dependency.getGroupId().equalsIgnoreCase(upgrade.oldGroupId()))
                    dependency.setGroupId(upgrade.newGroupId());

                if (upgrade.oldArtifactId() != null
                    && dependency.getArtifactId().equalsIgnoreCase(upgrade.oldArtifactId()))
                    dependency.setArtifactId(upgrade.newArtifactId());

                if (upgrade.oldVersion() != null
                    && dependency.getVersion().equalsIgnoreCase(upgrade.newVersion()))
                    dependency.setVersion(upgrade.newVersion());
            }

            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileOutputStream(pom.toFile()), model);
        } catch (IOException | XmlPullParserException e) {
            logger.error("Couldn't patch dependency in POM file", e);
        }
    }
}