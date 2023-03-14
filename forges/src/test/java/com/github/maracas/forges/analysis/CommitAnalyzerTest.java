package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.github.GitHubForge;
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

class CommitAnalyzerTest {
	final Path workingDirectory = Path.of("./clones");
	Forge forge;
	CommitAnalyzer analyzer;

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());
		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		analyzer = new CommitAnalyzer(workingDirectory);
		analyzer.setExecutorService(Executors.newFixedThreadPool(4));
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());
	}

	@Test
	void analyzeCommits_fixture_moduleA() {
		Commit v1 = forge.fetchCommit("alien-tools", "repository-fixture", "15b08c");
		Commit v2 = forge.fetchCommit("alien-tools", "repository-fixture", "b22087");
		Commit client1 = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		Commit client2 = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			List.of(
				new CommitBuilder(client1, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				new CommitBuilder(client2, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		assertThat(result.error(), is(emptyOrNullString()));
		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.delta().getBreakingChanges(), hasSize(1));

		BreakingChange bc = result.delta().getBreakingChanges().get(0);
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("a")));

		assertThat(result.deltaImpacts(), is(aMapWithSize(2)));

		DeltaImpact i1 = result.deltaImpacts().get(Path.of("./clones/ca"));
		assertThat(i1.getThrowable(), is(nullValue()));
		assertThat(i1.getBrokenUses(), hasSize(1));
		BrokenUse bu = i1.getBrokenUses().iterator().next();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("a.a()"));

		DeltaImpact i2 = result.deltaImpacts().get(Path.of("./clones/cb"));
		assertThat(i1.getThrowable(), is(nullValue()));
		assertThat(i2.getBrokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_fixture_nestedB() {
		Commit v1 = forge.fetchCommit("alien-tools", "repository-fixture", "15b08c");
		Commit v2 = forge.fetchCommit("alien-tools", "repository-fixture", "b22087");
		Commit client1 = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		Commit client2 = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-c/nested-b"))),
			new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-c/nested-b"))),
			List.of(
				new CommitBuilder(client1, workingDirectory.resolve("ca"), BuildConfig.newDefault()),
				new CommitBuilder(client2, workingDirectory.resolve("cb"), BuildConfig.newDefault())
			),
			MaracasOptions.newDefault()
		);

		assertThat(result.error(), is(emptyOrNullString()));
		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.delta().getBreakingChanges(), hasSize(1));

		BreakingChange bc = result.delta().getBreakingChanges().get(0);
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("nestedB")));

		assertThat(result.deltaImpacts(), is(aMapWithSize(2)));

		DeltaImpact i1 = result.deltaImpacts().get(Path.of("./clones/cb"));
		assertThat(i1.getThrowable(), is(nullValue()));
		assertThat(i1.getBrokenUses(), hasSize(1));
		BrokenUse bu = i1.getBrokenUses().iterator().next();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("nestedB.nestedB()"));

		DeltaImpact i2 = result.deltaImpacts().get(Path.of("./clones/ca"));
		assertThat(i1.getThrowable(), is(nullValue()));
		assertThat(i2.getBrokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_GumTree() {
		Commit v1 = forge.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = forge.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = forge.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core"))),
			new CommitBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core"))),
			Collections.singletonList(new CommitBuilder(client, workingDirectory.resolve("client"))),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
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
		AnalysisResult result = analyzer.computeImpact(
			delta,
			List.of(
				new CommitBuilder(client1, workingDirectory.resolve("client1")),
				new CommitBuilder(client2, workingDirectory.resolve("client2")),
				new CommitBuilder(client3, workingDirectory.resolve("client3"))
			),
			opts
		);

		assertThat(result.deltaImpacts(), is(aMapWithSize(3)));
		assertThat(result.deltaImpacts().values(), hasItem(hasProperty("throwable", hasProperty("message", containsString("timed out")))));
	}
}
