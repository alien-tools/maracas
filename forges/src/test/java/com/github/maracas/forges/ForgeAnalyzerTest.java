package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.GitHubForge;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForgeAnalyzerTest {
	final Path CLONES = Path.of(System.getProperty("java.io.tmpdir")).resolve("clones");
	Forge github;
	ForgeAnalyzer analyzer;

	@BeforeEach
	void setUp() throws IOException {
		github = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		FileUtils.deleteDirectory(CLONES.toFile());
		analyzer = new ForgeAnalyzer(github, CLONES);
	}

	@Test
	void analyzeCommits_CompChanges() throws Exception {
		Commit v1 = github.fetchCommit("alien-tools", "comp-changes", "089d612");
		Commit v2 = github.fetchCommit("alien-tools", "comp-changes", "a30d9d2");
		Commit client = github.fetchCommit("alien-tools", "comp-changes-client", "9741eb8");

		AnalysisResult result = analyzer.analyzeCommits(v1, v2, Collections.singletonList(client), MaracasOptions.newDefault());

		assertThat(result.delta().getBreakingChanges(), is(not(empty())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
		assertThat(result.allBrokenUses(), is(not(empty())));
	}

	@Test
	void analyzeCommits_GumTree() throws Exception {
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
	void computeDelta_GumTree_withTimeout() {
		Commit v1 = github.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = github.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		CommitBuilder cb1 = new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		analyzer.setLibraryBuildTimeoutSeconds(1);
		Exception thrown = assertThrows(CompletionException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getCause(), is(instanceOf(TimeoutException.class)));
	}

	@Test
	void computeImpact_GumTree_withTimeout() throws Exception {
		Commit v1 = github.fetchCommit("GumTreeDiff", "gumtree", "2570d34");
		Commit v2 = github.fetchCommit("GumTreeDiff", "gumtree", "7925aa5");
		Commit client = github.fetchCommit("SpoonLabs", "gumtree-spoon-ast-diff", "6533706");
		MaracasOptions opts = MaracasOptions.newDefault();

		analyzer.setClientAnalysisTimeoutSeconds(1);
		Delta delta = analyzer.computeDelta(
			new CommitBuilder(v1, CLONES.resolve("v1"), new BuildConfig(Path.of("core"))),
			new CommitBuilder(v2, CLONES.resolve("v2"), new BuildConfig(Path.of("core"))),
			opts
		);

		AnalysisResult result = analyzer.computeImpact(
			delta,
			Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"))),
			MaracasOptions.newDefault()
		);

		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));

		DeltaImpact impact = result.deltaImpacts().values().stream().findAny().get();
		assertThat(impact.getThrowable(), is(instanceOf(TimeoutException.class)));
		assertThat(impact.getBrokenUses(), is(empty()));
	}

	@Test
	void analyzePullRequest_apache_dubbo_10741() throws Exception {
		PullRequest pr = github.fetchPullRequest("apache", "dubbo", 10741);
		List<AnalysisResult> results = analyzer.analyzePullRequest(pr, 2, MaracasOptions.newDefault());
		System.out.println(results);

		results.forEach(res -> System.out.println(res));

		assertThat(results, is(not(empty())));
	}
}
