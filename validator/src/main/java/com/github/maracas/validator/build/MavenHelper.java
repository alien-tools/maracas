package com.github.maracas.validator.build;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
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
     * Group ID of the Maven Compiler Plugin
     */
    private static final String MVN_COMPILER_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    /**
     * Artifact ID of the Maven Compiler Plugin
     */
    private static final String MVN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";

    /**
     * Version of the Maven Compiler Plugin
     */
    private static final String MVN_COMPILER_PLUGIN_VERSION = "3.10.0";

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
        try {
            Model model = createModel(src, relativePOMPath);
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

            writeModel(model, src, relativePOMPath);
        } catch (IOException | XmlPullParserException e) {
            logger.error("Couldn't patch dependency in POM file", e);
        }
    }

    /**
     * Increases the number of error and warning messages reported by the Maven
     * compiler.
     *
     * @param src             absolute path to the source project
     * @param relativePOMPath relative path to the POM file in the source project
     * @param messages        maximum number of messages to report
     */
    public static void increaseMaxMessages(Path src, String relativePOMPath, int messages) {
        try {
            Model model = createModel(src, relativePOMPath);
            logger.info("Increasing Xmaxerrs and Xmaxwarns to {} in POM file", messages);

            Plugin compilerPlugin;
            Map<String, Plugin> plugins = model.getBuild().getPluginsAsMap();
            String key = String.format("%s:%s", MVN_COMPILER_PLUGIN_GROUP_ID,
                MVN_COMPILER_PLUGIN_ARTIFACT_ID);

            if (!plugins.containsKey(key)) {
                compilerPlugin = new Plugin();
                compilerPlugin.setArtifactId(MVN_COMPILER_PLUGIN_ARTIFACT_ID);
                compilerPlugin.setGroupId(MVN_COMPILER_PLUGIN_GROUP_ID);
                compilerPlugin.setVersion(MVN_COMPILER_PLUGIN_VERSION);
                model.getBuild().addPlugin(compilerPlugin);
            } else {
                compilerPlugin = plugins.get(key);
            }

            if (compilerPlugin.getConfiguration() == null)
                compilerPlugin.setConfiguration(new Xpp3Dom("configuration"));

            Xpp3Dom config = (Xpp3Dom) compilerPlugin.getConfiguration();
            Xpp3Dom compilerArgs = addDomNodeChild(config, "compilerArguments", null);
            addDomNodeChild(compilerArgs, "Xmaxerrs", String.valueOf(messages));
            addDomNodeChild(compilerArgs, "Xmaxwarns", String.valueOf(messages));
            writeModel(model, src, relativePOMPath);
        } catch (IOException | XmlPullParserException e) {
            logger.error("Couldn't patch dependency in POM file", e);
        }
    }

    /**
     * Adds a child node to a {@link Xpp3Dom} node. It considers the name and
     * value of the node.
     *
     * @param parent {@link Xpp3Dom} parent node
     * @param name   name of the child node
     * @param value  value of the child node (can be null)
     * @return added child node
     */
    private static Xpp3Dom addDomNodeChild(Xpp3Dom parent, String name, String value) {
        Xpp3Dom child = parent.getChild(name);
        if (child == null) {
            child = new Xpp3Dom(name);
            parent.addChild(child);
        }
        if (value != null)
            child.setValue(value);

        return child;
    }
    /**
     * Creates a POM file model given the path to the source project and the
     * relative path of the POM file.
     *
     * @param src             absolute path to the source project
     * @param relativePOMPath relative path to the POM file in the source project
     * @return {@link Model} of the project POM file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static Model createModel(Path src, String relativePOMPath) throws FileNotFoundException, IOException, XmlPullParserException {
        if (relativePOMPath == null)
            relativePOMPath = "pom.xml";

        Path pom = src.resolve(relativePOMPath);
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = pomReader.read(new FileInputStream(pom.toFile()));
        return model;
    }

    /**
     * Writes the POM model as a file given the path to the source project and
     * the relative path of the POM file.
     *
     * @param model           POM file model to write
     * @param src             absolute path to the source project
     * @param relativePOMPath relative path to the POM file in the source project
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void writeModel(Model model, Path src, String relativePOMPath) throws FileNotFoundException, IOException {
        if (relativePOMPath == null)
            relativePOMPath = "pom.xml";

        Path pom = src.resolve(relativePOMPath);
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(pom.toFile()), model);
    }
}
