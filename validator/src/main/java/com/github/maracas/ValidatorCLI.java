package com.github.maracas;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.maracas.build.BuildHandler;
import com.github.maracas.build.MavenBuildHandler;

public class ValidatorCLI {
    public static void main(String[] args) {
        Path src = Paths.get("/home/lina/Documents/code/maracas/test-data/comp-changes/client");
        BuildHandler handler = new MavenBuildHandler(src);
        handler.build();
    }
}
