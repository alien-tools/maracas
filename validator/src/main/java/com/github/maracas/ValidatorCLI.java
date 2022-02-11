package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.build.BuildHandler;
import com.github.maracas.build.CompilerMessage;
import com.github.maracas.build.MavenBuildHandler;
import com.github.maracas.delta.Delta;
import com.github.maracas.matchers.LocationMatcher;
import com.github.maracas.matchers.AccuracyCase;
import com.github.maracas.matchers.Matcher;

public class ValidatorCLI {
    public static void main(String[] args) {
        Path src = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/client");
        Path jar1 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
        Path jar2 = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");

        BuildHandler handler = new MavenBuildHandler(src);
        Delta delta = Maracas.computeDelta(jar1, jar2);
        Collection<BrokenUse> brokenUses = Maracas.computeBrokenUses(src, delta);
        List<CompilerMessage> messages = handler.gatherCompilerMessages();

        Matcher matcher = new LocationMatcher();
        Collection<AccuracyCase> data = matcher.match(brokenUses, messages);
        var i = 0;

        //handler.build();
//        for (CompilerMessage msg : handler.gatherCompilerMessages()) {
//            System.out.println(msg.toString());
//        }
    }
}
