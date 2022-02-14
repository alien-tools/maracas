package com.github.maracas;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

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

public class MaracasValidatorCLI {
    public static void main(String[] args) {
        Path src = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/client");
        Path api1 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/old");
        Path api2 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/new");

        // Generate jar in target folder
        packageMavenProject(api1);
        packageMavenProject(api2);

        // Get path of the previously generated jars
        //Path jar1 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
        //Path jar2 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
        Path jar1 = getJarPath(api1, null);
        Path jar2 = getJarPath(api2, null);

        BuildHandler handler = new MavenBuildHandler(src);
        Delta delta = Maracas.computeDelta(jar1, jar2);
        Collection<BrokenUse> brokenUses = Maracas.computeBrokenUses(src, delta);
        List<CompilerMessage> messages = handler.gatherCompilerMessages();

        Matcher matcher = new LocationMatcher();
        Collection<AccuracyCase> cases = matcher.match(brokenUses, messages);
        AccuracyAnalyzer analyzer = new AccuracyAnalyzer(cases);

        System.out.println(String.format("Precision: %s", analyzer.precision()));
        System.out.println(String.format("Recall: %s", analyzer.recall()));
        System.out.println(String.format("TP: %s", analyzer.truePositives().size()));
        System.out.println(String.format("FP: %s", analyzer.falsePositives().size()));
        System.out.println(String.format("FN: %s", analyzer.falseNegatives().size()));

        //handler.build();
//        for (CompilerMessage msg : handler.gatherCompilerMessages()) {
//            System.out.println(msg.toString());
//        }
    }

    private static void packageMavenProject(Path src) {
        MavenBuildConfig config = new MavenBuildConfig(src.toString(),
            null, List.of("clean", "package"), null);
        BuildHandler handler = new MavenBuildHandler(src, config);
        handler.build();
    }

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
            System.out.println(e);
        }
        return null;
    }
}
