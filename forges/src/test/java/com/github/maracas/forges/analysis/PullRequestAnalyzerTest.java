package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.GitHubForge;
import japicmp.model.JApiCompatibilityChange;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class PullRequestAnalyzerTest {
  final Path workingDirectory = Path.of("./test-clones");
  Forge forge;
  PullRequestAnalyzer analyzer;

  @BeforeEach
  void setUp() throws IOException {
    FileUtils.deleteDirectory(workingDirectory.toFile());
    forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
    CommitAnalyzer commitAnalyzer = new CommitAnalyzer(Executors.newFixedThreadPool(4));
    analyzer = new PullRequestAnalyzer(workingDirectory, forge, commitAnalyzer);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(workingDirectory.toFile());
  }

  @Test
  void analyzePullRequest_fixture_two_broken_modules() {
    PullRequest pr = forge.fetchPullRequest("alien-tools", "repository-fixture", 1);
    Repository clientA = forge.fetchRepository("alien-tools", "client-fixture-a");
    Repository clientB = forge.fetchRepository("alien-tools", "client-fixture-b");
    PullRequestAnalysisResult results = analyzer.analyze(pr, MaracasOptions.newDefault());
    assertThat(results.packageResults(), aMapWithSize(2));

    PackageAnalysisResult resultA = results.packageResults().get("com.github.alien-tools:module-a");
    PackageAnalysisResult resultB = results.packageResults().get("com.github.alien-tools:nested-b");
    assertThat(resultA, is(not(nullValue())));
    assertThat(resultB, is(not(nullValue())));

    // module-a
    assertThat(resultA.error(), is(emptyOrNullString()));
    assertThat(resultA.delta(), is(not(nullValue())));
    assertThat(resultA.delta().getBreakingChanges(), hasSize(1));

    BreakingChange bca = resultA.delta().getBreakingChanges().get(0);
    assertThat(bca.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
    assertThat(bca.getReference().getSimpleName(), is(equalTo("a")));

    assertThat(resultA.clientResults(), is(aMapWithSize(2)));

    DeltaImpact ia1 = resultA.clientResults().get(clientA);
    assertThat(ia1.getThrowable(), is(nullValue()));
    assertThat(ia1.getBrokenUses(), hasSize(1));
    BrokenUse bua = ia1.getBrokenUses().iterator().next();
    assertThat(bua.use(), is(APIUse.METHOD_INVOCATION));
    assertThat(bua.element().toString(), is("a.a()"));

    DeltaImpact ia2 = resultA.clientResults().get(clientB);
    assertThat(ia2.getThrowable(), is(nullValue()));
    assertThat(ia2.getBrokenUses(), is(empty()));

    // nested-b
    assertThat(resultB.error(), is(emptyOrNullString()));
    assertThat(resultB.delta(), is(not(nullValue())));
    assertThat(resultB.delta().getBreakingChanges(), hasSize(1));

    BreakingChange bcb = resultB.delta().getBreakingChanges().get(0);
    assertThat(bcb.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
    assertThat(bcb.getReference().getSimpleName(), is(equalTo("nestedB")));

    assertThat(resultB.clientResults(), is(aMapWithSize(2)));

    DeltaImpact ib1 = resultB.clientResults().get(clientB);
    assertThat(ib1.getThrowable(), is(nullValue()));
    assertThat(ib1.getBrokenUses(), hasSize(1));
    BrokenUse bub = ib1.getBrokenUses().iterator().next();
    assertThat(bub.use(), is(APIUse.METHOD_INVOCATION));
    assertThat(bub.element().toString(), is("nestedB.nestedB()"));

    DeltaImpact ib2 = resultB.clientResults().get(clientA);
    assertThat(ib2.getThrowable(), is(nullValue()));
    assertThat(ib2.getBrokenUses(), is(empty()));
  }

  @Test
  void inferImpactedPackages_fixture_two_impacted_modules() {
    PullRequest pr = forge.fetchPullRequest("alien-tools", "repository-fixture", 1);
    CommitBuilder baseBuilder = new CommitBuilder(pr.mergeBase(), workingDirectory.resolve("v1"), BuildConfig.newDefault());
    List<BuildModule> impacted = analyzer.inferImpactedPackages(pr, baseBuilder, MaracasOptions.newDefault());

    assertThat(impacted, containsInAnyOrder(
        new BuildModule("com.github.alien-tools:module-a", Path.of("module-a")),
        new BuildModule("com.github.alien-tools:nested-b", Path.of("module-c/nested-b"))
    ));
  }

  @Test
  void inferImpactedPackages_fixture_no_impacted_module() {
    PullRequest pr = forge.fetchPullRequest("alien-tools", "repository-fixture", 2);
    CommitBuilder baseBuilder = new CommitBuilder(pr.mergeBase(), workingDirectory.resolve("v1"), BuildConfig.newDefault());
    List<BuildModule> impacted = analyzer.inferImpactedPackages(pr, baseBuilder, MaracasOptions.newDefault());

    assertThat(impacted, is(empty()));
  }

  @Test
  void inferImpactedPackages_fixture_one_impacted_module() {
    PullRequest pr = forge.fetchPullRequest("alien-tools", "repository-fixture", 4);
    CommitBuilder baseBuilder = new CommitBuilder(pr.mergeBase(), workingDirectory.resolve("v1"), BuildConfig.newDefault());
    List<BuildModule> impacted = analyzer.inferImpactedPackages(pr, baseBuilder, MaracasOptions.newDefault());

    assertThat(impacted, contains(new BuildModule("com.github.alien-tools:module-a", Path.of("module-a"))));
  }
}