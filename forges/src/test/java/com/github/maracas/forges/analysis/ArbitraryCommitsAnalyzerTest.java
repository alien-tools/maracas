package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuilderFactory;
import com.github.maracas.forges.build.CommitBuilderFactory;
import com.github.maracas.forges.clone.ClonerFactory;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.CommitsReport;
import com.github.maracas.forges.report.ForgeBreakingChange;
import com.github.maracas.forges.report.ForgeBrokenUse;
import japicmp.model.JApiCompatibilityChange;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ArbitraryCommitsAnalyzerTest {
	final Path workingDirectory = Path.of("./clones");
	Forge forge;
	Commit fixtureV1, fixtureV2, clientA, clientB;

	CommitAnalyzer commitAnalyzer;
	ArbitraryCommitsAnalyzer arbitraryCommitsAnalyzer;
	BuilderFactory builderFactory;
	ClonerFactory clonerFactory;
	CommitBuilderFactory commitBuilderFactory;

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());

		builderFactory = new BuilderFactory();
		clonerFactory = new ClonerFactory();
		commitBuilderFactory = new CommitBuilderFactory(clonerFactory, builderFactory);
		commitAnalyzer = new ParallelCommitAnalyzer();
		arbitraryCommitsAnalyzer = new ArbitraryCommitsAnalyzer(commitAnalyzer);
		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());

		Repository fixture = forge.fetchRepository("alien-tools", "repository-fixture");
		fixtureV1 = forge.fetchCommit(fixture, "15b08c");
		fixtureV2 = forge.fetchCommit(fixture, "b22087");
		clientA = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		clientB = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");
	}

	@Test
	void analyzeCommits_fixture_moduleA() {
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				commitBuilderFactory.createClientBuilder(clientA, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				commitBuilderFactory.createClientBuilder(clientB, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		//Mockito.verify(builderFactory, Mockito.times(4)).create(fixtureV1.repository());

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
		assertThat(i2.error(), is(nullValue()));
		assertThat(i2.client(), is(equalTo(clientB)));
		assertThat(i2.brokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_fixture_nestedB() {
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-c/nested-b"))),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-c/nested-b"))),
			List.of(
				commitBuilderFactory.createClientBuilder(clientA, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				commitBuilderFactory.createClientBuilder(clientB, workingDirectory.resolve("cb"), BuildConfig.newDefault())
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
	void analyzeCommits_fixture_moduleA_noImpact() {
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				commitBuilderFactory.createClientBuilder(clientB, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		assertThat(report.clientsImpact(), hasSize(1));
		ClientImpact client = report.clientsImpact().get(0);
		assertThat(client.brokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_fixture_unknownLibraryModule() {
		BuildConfig unknownModule = new BuildConfig(Path.of("unknown"));
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), unknownModule),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
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
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), wrongGoal),
			Collections.emptyList(),
			MaracasOptions.newDefault()
		);

		assertThat(report.delta(), is(nullValue()));
		assertThat(report.error(), containsString("Unknown lifecycle phase \"unknown\""));
	}

	@Test
	void analyzeCommits_fixture_unknownClientModule() {
		BuildConfig unknownModule = new BuildConfig(Path.of("unknown"));
		CommitsReport report = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(fixtureV1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			commitBuilderFactory.createLibraryBuilder(fixtureV2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				commitBuilderFactory.createClientBuilder(clientA, workingDirectory.resolve("ca"), unknownModule)
			),
			MaracasOptions.newDefault()
		);

		assertThat(report.delta(), is(nullValue()));
		assertThat(report.error(), containsString("Not a valid source directory: ./clones/ca/unknown"));
	}

	@Test
	void analyzeCommits_GumTree() {
		Commit v1 = forge.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = forge.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = forge.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");

		CommitsReport result = arbitraryCommitsAnalyzer.analyzeCommits(
			commitBuilderFactory.createLibraryBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core"))),
			commitBuilderFactory.createLibraryBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core"))),
			Collections.singletonList(commitBuilderFactory.createClientBuilder(client, workingDirectory.resolve("client"), BuildConfig.newDefault())),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.error(), is(nullValue()));
		assertThat(result.clientsImpact(), hasSize(1));
	}
}