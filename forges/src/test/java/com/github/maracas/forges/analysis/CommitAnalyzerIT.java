package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommitAnalyzerIT {
	Forge forge;
	CommitAnalyzer analyzer;

	@BeforeEach
	void setUp() throws IOException {
		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		analyzer = new CommitAnalyzer(new Maracas());
		FileUtils.deleteQuietly(Path.of("clones").toFile());
	}

	@Test
	void analyzeCommits_fixture_moduleA() {
		Commit v1 = forge.fetchCommit("alien-tools", "repository-fixture", "15b08c");
		Commit v2 = forge.fetchCommit("alien-tools", "repository-fixture", "b22087");
		Commit client1 = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		Commit client2 = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(v2, new BuildConfig(Path.of("module-a"))),
			List.of(
				new CommitBuilder(client1),
				new CommitBuilder(client2)
			),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.delta().getBreakingChanges(), hasSize(1));

		BreakingChange bc = result.delta().getBreakingChanges().get(0);
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("a")));

		assertThat(result.deltaImpacts(), is(aMapWithSize(2)));

		DeltaImpact i1 = result.deltaImpacts().keySet().stream()
				.filter(c -> c.getLocation().toString().contains("client-fixture-a"))
				.findFirst()
				.map(result.deltaImpacts()::get)
				.get();
		assertThat(i1.throwable(), is(nullValue()));
		assertThat(i1.brokenUses(), hasSize(1));
		BrokenUse bu = i1.brokenUses().iterator().next();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("a.a()"));

		DeltaImpact i2 = result.deltaImpacts().keySet().stream()
			.filter(c -> c.getLocation().toString().contains("client-fixture-b"))
			.findFirst()
			.map(result.deltaImpacts()::get)
			.get();
		assertThat(i2.throwable(), is(nullValue()));
		assertThat(i2.brokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_fixture_nestedB() {
		Commit v1 = forge.fetchCommit("alien-tools", "repository-fixture", "15b08c");
		Commit v2 = forge.fetchCommit("alien-tools", "repository-fixture", "b22087");
		Commit client1 = forge.fetchCommit("alien-tools", "client-fixture-a", "0720ff");
		Commit client2 = forge.fetchCommit("alien-tools", "client-fixture-b", "d0718e");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, new BuildConfig(Path.of("module-c/nested-b"))),
			new CommitBuilder(v2, new BuildConfig(Path.of("module-c/nested-b"))),
			List.of(
				new CommitBuilder(client1),
				new CommitBuilder(client2)
			),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.delta().getBreakingChanges(), hasSize(1));

		BreakingChange bc = result.delta().getBreakingChanges().get(0);
		assertThat(bc.getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(bc.getReference().getSimpleName(), is(equalTo("nestedB")));

		assertThat(result.deltaImpacts(), is(aMapWithSize(2)));

		DeltaImpact i1 = result.deltaImpacts().keySet().stream()
			.filter(c -> c.getLocation().toString().contains("client-fixture-b"))
			.findFirst()
			.map(result.deltaImpacts()::get)
			.get();
		assertThat(i1.throwable(), is(nullValue()));
		assertThat(i1.brokenUses(), hasSize(1));
		BrokenUse bu = i1.brokenUses().iterator().next();
		assertThat(bu.use(), is(APIUse.METHOD_INVOCATION));
		assertThat(bu.element().toString(), is("nestedB.nestedB()"));

		DeltaImpact i2 = result.deltaImpacts().keySet().stream()
			.filter(c -> c.getLocation().toString().contains("client-fixture-a"))
			.findFirst()
			.map(result.deltaImpacts()::get)
			.get();
		assertThat(i2.throwable(), is(nullValue()));
		assertThat(i2.brokenUses(), is(empty()));
	}

	@Test
	void analyzeCommits_GumTree() {
		Commit v1 = forge.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = forge.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = forge.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, new BuildConfig(Path.of("core"))),
			new CommitBuilder(v2, new BuildConfig(Path.of("core"))),
			Collections.singletonList(new CommitBuilder(client)),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
	}

	@Test
	void computeDelta_maracas_withBuildTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		opts.setBuildTimeoutSeconds(1);
		Exception thrown = assertThrows(BuildException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeDelta_maracas_withCloneTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, new BuildConfig(Path.of("core")));
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
			new CommitBuilder(v1, new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(v2, new BuildConfig(Path.of("module-a"))),
			opts
		);

		opts.setCloneTimeoutSeconds(2);
		AnalysisResult result = analyzer.computeImpact(
			delta,
			List.of(
				new CommitBuilder(client1),
				new CommitBuilder(client2),
				new CommitBuilder(client3)
			),
			opts
		);

		assertThat(result.deltaImpacts(), is(aMapWithSize(3)));
		assertTrue(result.deltaImpacts().values().stream().anyMatch(i -> i.throwable() != null && i.throwable().getMessage().contains("timed out")));
	}
}
