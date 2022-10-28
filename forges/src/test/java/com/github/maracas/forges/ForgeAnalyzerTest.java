package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.github.GitHubForge;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForgeAnalyzerTest {
	final Path CLONES = Path.of("./clones");
	Forge github;
	ForgeAnalyzer analyzer;

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(CLONES.toFile());
		github = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		analyzer = new ForgeAnalyzer(github, CLONES);
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(CLONES.toFile());
	}

	@Test
	void analyzeCommits_CompChanges() {
		Commit v1 = github.fetchCommit("alien-tools", "comp-changes", "089d612");
		Commit v2 = github.fetchCommit("alien-tools", "comp-changes", "a30d9d2");
		Commit client = github.fetchCommit("alien-tools", "comp-changes-client", "9741eb8");

		AnalysisResult result = analyzer.analyzeCommits(v1, v2, Collections.singletonList(client), MaracasOptions.newDefault());

		assertThat(result.delta().getBreakingChanges(), is(not(empty())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
		assertThat(result.allBrokenUses(), is(not(empty())));
	}

	@Test
	void analyzeCommits_GumTree() {
		Commit v1 = github.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = github.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = github.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");

		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("core"))),
			new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("core"))),
			Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"))),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
	}

	@Test
	void computeDelta_maracas_withBuildTimeout() {
		Commit v1 = github.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = github.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		analyzer.setBuildTimeoutSeconds(1);
		Exception thrown = assertThrows(BuildException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeDelta_maracas_withCloneTimeout() {
		Commit v1 = github.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = github.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		analyzer.setCloneTimeoutSeconds(1);
		Exception thrown = assertThrows(CloneException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeImpact_fixture_withCloneTimeout() {
		Commit v1 = github.fetchCommit("alien-tools", "repository-fixture", "15b08c0");
		Commit v2 = github.fetchCommit("alien-tools", "repository-fixture", "b220873");
		Commit client1 = github.fetchCommit("alien-tools", "client-fixture-a", "HEAD");
		Commit client2 = github.fetchCommit("alien-tools", "client-fixture-b", "HEAD");
		Commit client3 = github.fetchCommit("alien-tools", "maracas", "HEAD");
		MaracasOptions opts = MaracasOptions.newDefault();

		Delta delta = analyzer.computeDelta(
			new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			opts
		);

		analyzer.setCloneTimeoutSeconds(2);
		AnalysisResult result = analyzer.computeImpact(
			delta,
			List.of(
				new CommitBuilder(client1, CLONES.resolve("client1")),
				new CommitBuilder(client2, CLONES.resolve("client2")),
				new CommitBuilder(client3, CLONES.resolve("client3"))
			),
			MaracasOptions.newDefault()
		);

		assertThat(result.deltaImpacts(), is(aMapWithSize(3)));
		assertThat(result.deltaImpacts().values(), hasItem(hasProperty("throwable", hasProperty("message", containsString("timed out")))));
	}
}
