package com.github.maracas.forges;

import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.CommitsReport;
import com.github.maracas.forges.report.ForgeBreakingChange;
import com.github.maracas.forges.report.ForgeBrokenUse;
import com.github.maracas.forges.report.PullRequestReport;
import japicmp.model.JApiCompatibilityChange;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeAnalyzerTest {
	Path workingDirectory = Path.of("./clones");
	Forge forge;
	ForgeAnalyzer analyzer;

	Repository fixture;
	Commit fixtureV1, fixtureV2, clientA, clientB;
	PullRequest pr1;

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());
		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		analyzer = new ForgeAnalyzer(forge, workingDirectory);
		analyzer.setExecutorService(Executors.newFixedThreadPool(4));
		fixture = forge.fetchRepository("alien-tools", "repository-fixture");
		fixtureV1 = forge.fetchCommit(fixture, "15b08c");
		fixtureV2 = forge.fetchCommit(fixture, "b22087");
		clientA = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		clientB = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");
		pr1 = forge.fetchPullRequest(fixture, 1);
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());
	}

	@Test
	void analyzeCommits_fixture_moduleA() {
		CommitsReport report = analyzer.analyzeCommits(
			new CommitBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				new CommitBuilder(clientA, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				new CommitBuilder(clientB, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		assertThat(report.error(), is(emptyOrNullString()));
		assertThat(report.delta(), is(not(nullValue())));
		assertThat(report.delta().breakingChanges(), hasSize(1));

		ForgeBreakingChange fbc = report.delta().breakingChanges().get(0);
		BreakingChange bc = fbc.breakingChange();
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("a")));

		assertThat(report.clientsImpact(), hasSize(2));

		ClientImpact i1 = report.clientsImpact().get(0);
		assertThat(i1.error(), is(nullValue()));
		assertThat(i1.client(), is(equalTo(clientA)));
		assertThat(i1.brokenUses(), hasSize(1));
		ForgeBrokenUse fbu = i1.brokenUses().get(0);
		BrokenUse bu = fbu.brokenUse();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("a.a()"));

		ClientImpact i2 = report.clientsImpact().get(1);
		assertThat(i1.error(), is(nullValue()));
		assertThat(i2.client(), is(equalTo(clientB)));
		assertThat(i2.brokenUses(), is(empty()));

		System.out.println(report);
	}

	@Test
	void analyzeCommits_fixture_nestedB() {
		CommitsReport report = analyzer.analyzeCommits(
			new CommitBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-c/nested-b"))),
			new CommitBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-c/nested-b"))),
			List.of(
				new CommitBuilder(clientA, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				new CommitBuilder(clientB, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		assertThat(report.error(), is(emptyOrNullString()));
		assertThat(report.delta(), is(not(nullValue())));
		assertThat(report.delta().breakingChanges(), hasSize(1));

		ForgeBreakingChange fbc = report.delta().breakingChanges().get(0);
		BreakingChange bc = fbc.breakingChange();
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("nestedB")));

		assertThat(report.clientsImpact(), hasSize(2));

		ClientImpact i1 = report.clientsImpact().get(0);
		assertThat(i1.error(), is(nullValue()));
		assertThat(i1.client(), is(equalTo(clientA)));
		assertThat(i1.brokenUses(), is(empty()));

		ClientImpact i2 = report.clientsImpact().get(1);
		assertThat(i2.error(), is(nullValue()));
		assertThat(i2.client(), is(equalTo(clientB)));
		assertThat(i2.brokenUses(), hasSize(1));
		ForgeBrokenUse fbu = i2.brokenUses().get(0);
		BrokenUse bu = fbu.brokenUse();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("nestedB.nestedB()"));
	}

	@Test
	void analyzeCommits_fixture_unknownLibraryModule() {
		BuildConfig unknownModule = new BuildConfig(Path.of("unknown"));
		CommitsReport report = analyzer.analyzeCommits(
			new CommitBuilder(fixtureV1, workingDirectory.resolve("v1"), unknownModule),
			new CommitBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			Collections.emptyList(),
			MaracasOptions.newDefault()
		);

		assertThat(report.delta(), is(nullValue()));
		assertThat(report.error(), containsString("Couldn't find module unknown"));
	}

	@Test
	void analyzeCommits_fixture_compilationFails() {
		BuildConfig wrongGoal = new BuildConfig(Path.of("module-a"));
		wrongGoal.addGoal("unknown");
		CommitsReport report = analyzer.analyzeCommits(
			new CommitBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(fixtureV2, workingDirectory.resolve("v2"), wrongGoal),
			Collections.emptyList(),
			MaracasOptions.newDefault()
		);

		assertThat(report.delta(), is(nullValue()));
		assertThat(report.error(), containsString("Unknown lifecycle phase \"unknown\""));
	}

	@Test
	void analyzeCommits_fixture_unknownClientModule() {
		BuildConfig unknownModule = new BuildConfig(Path.of("unknown"));
		CommitsReport report = analyzer.analyzeCommits(
			new CommitBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				new CommitBuilder(clientA, workingDirectory.resolve("ca"), unknownModule)
			),
			MaracasOptions.newDefault()
		);

		assertThat(report.delta(), is(nullValue()));
		assertThat(report.error(), containsString("Not a valid source directory: ./clones/ca/unknown"));
	}

	@Test
	void analyzePullRequest_fixture_1() {
		PullRequestReport report = analyzer.analyzePullRequest(pr1, MaracasOptions.newDefault());
		System.out.println(report);
	}

	@Test
	void analyzeCommits_GumTree() {
		Commit v1 = forge.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = forge.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = forge.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");

		CommitsReport result = analyzer.analyzeCommits(
			new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core"))),
			new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core"))),
			Collections.singletonList(new CommitBuilder(client, workingDirectory.resolve("client"))),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.error(), is(nullValue()));
		assertThat(result.clientsImpact(), hasSize(1));
	}

	@Test
	void computeDelta_maracas_withBuildTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		opts.setBuildTimeoutSeconds(1);
		Exception thrown = assertThrows(BuildException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeDelta_maracas_withCloneTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		opts.setCloneTimeoutSeconds(1);
		Exception thrown = assertThrows(CloneException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeImpact_fixture_withCloneTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "repository-fixture", "15b08c0");
		Commit v2 = forge.fetchCommit("alien-tools", "repository-fixture", "b220873");
		Commit client1 = forge.fetchCommit("alien-tools", "client-fixture-a", "HEAD");
		Commit client2 = forge.fetchCommit("alien-tools", "client-fixture-b", "HEAD");
		Commit client3 = forge.fetchCommit("torvalds", "linux", "HEAD");
		MaracasOptions opts = MaracasOptions.newDefault();

		Delta delta = analyzer.computeDelta(
			new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			opts
		);

		opts.setCloneTimeoutSeconds(2);
		List<ClientImpact> result = analyzer.computeImpact(
			delta,
			List.of(
				new CommitBuilder(client1, workingDirectory.resolve("client1")),
				new CommitBuilder(client2, workingDirectory.resolve("client2")),
				new CommitBuilder(client3, workingDirectory.resolve("client3"))
			),
			opts
		);

		assertThat(result, hasSize(3));
		assertTrue(result.stream().anyMatch(impact -> impact.error() != null && impact.error().contains("timed out")));
	}
}
