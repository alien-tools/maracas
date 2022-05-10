package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.build.BuildConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(CLONES.toFile());
  }

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
    Commit v1 = new Commit(compChanges, "089d6129244bca3f13686e53c5968dea9c882613");
    Commit v2 = new Commit(compChanges, "a30d9d23a2d61776aeef88aa327753bd45b991a6");
    Path v1Clone = CLONES.resolve("v1");
    Path v2Clone = CLONES.resolve("v2");
    Commit client = new Commit(compChangesClient, "9741eb8a8f9d9c92dc4d0125acbe64c1265cb07b");

    ForgeAnalyzer analyzer = new ForgeAnalyzer();
    AnalysisResult result = analyzer.analyzeCommits(
      new CommitBuilder(v1, v1Clone, new BuildConfig(v1Clone)),
      new CommitBuilder(v2, v2Clone, new BuildConfig(v2Clone)),
      Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"), Paths.get(""))),
      MaracasOptions.newDefault()
    );

    assertThat(result.delta().getBreakingChanges(), is(not(empty())));
    assertThat(result.deltaImpacts().keySet(), hasSize(1));
    assertThat(result.allBrokenUses(), is(not(empty())));
  }
}
