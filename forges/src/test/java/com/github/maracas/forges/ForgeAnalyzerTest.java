package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.build.BuildConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

class ForgeAnalyzerTest {
  final Path CLONES = Paths.get(System.getProperty("java.io.tmpdir")).resolve("clones");

  @Test
  void analyzeCommits_CompChanges() throws InterruptedException, ExecutionException {
    Repository compChanges = new Repository(
      "alien-tools",
      "comp-changes",
      "https://github.com/alien-tools/comp-changes.git",
      "main"
    );
    Repository compChangesClient = new Repository(
      "alien-tools",
      "comp-change-clients",
      "https://github.com/alien-tools/comp-changes-client.git",
      "main"
    );
    Commit v1 = new Commit(compChanges, "ac4e0e53af92cc5fbb45549c113b6626d4f982d2");
    Commit v2 = new Commit(compChanges, "6c19cc73f549a71f5c8a808f336883d3a7a981f3");
    Path v1Clone = CLONES.resolve("v1");
    Path v2Clone = CLONES.resolve("v2");
    Commit client = new Commit(compChangesClient, "6a019052348cd8916c17ffcce67b16d5dbfe0a4f");

    ForgeAnalyzer analyzer = new ForgeAnalyzer();
    AnalysisResult result = analyzer.analyzeCommits(
      new CommitBuilder(v1, v1Clone, new BuildConfig(v1Clone)),
      new CommitBuilder(v2, v2Clone, new BuildConfig(v2Clone)),
      Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"))),
      MaracasOptions.newDefault()
    );

    assertThat(result.delta().getBreakingChanges(), is(not(empty())));
    assertThat(result.deltaImpacts().keySet(), hasSize(1));
    assertThat(result.allBrokenUses(), is(not(empty())));
  }
}
