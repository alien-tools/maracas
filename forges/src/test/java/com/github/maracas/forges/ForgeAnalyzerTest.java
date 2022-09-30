package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForgeAnalyzerTest {
	final Path CLONES = Path.of(System.getProperty("java.io.tmpdir")).resolve("clones");

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
			Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"), Path.of(""))),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta().getBreakingChanges(), is(not(empty())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
		assertThat(result.allBrokenUses(), is(not(empty())));
	}

	@Test
	void analyzeCommits_GumTree() throws InterruptedException, ExecutionException {
		Repository gumtree = new Repository(
			"GumTreeDiff",
			"gumtree",
			"https://github.com/GumTreeDiff/gumtree.git",
			"main"
		);
		Repository gumtreeClient = new Repository(
			"SpoonLabs",
			"gumtree-spoon-ast-diff",
			"https://github.com/SpoonLabs/gumtree-spoon-ast-diff.git",
			"master"
		);
		Commit v1 = new Commit(gumtree, "2570d34d35daf22e7778c564f19df301433ded21");
		Commit v2 = new Commit(gumtree, "7925aa5e0e7a221e56b5c83de5156034a8ff394f");
		Path v1Clone = CLONES.resolve("v1");
		Path v2Clone = CLONES.resolve("v2");
		Commit client = new Commit(gumtreeClient, "6533706e98ba0b7be088a2933641aeee3c458c85");

		ForgeAnalyzer analyzer = new ForgeAnalyzer();
		AnalysisResult result = analyzer.analyzeCommits(
			new CommitBuilder(v1, v1Clone, new BuildConfig(v1Clone, Path.of("core"))),
			new CommitBuilder(v2, v2Clone, new BuildConfig(v2Clone, Path.of("core"))),
			Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"), Path.of(""))),
			MaracasOptions.newDefault()
		);

		assertThat(result.delta(), is(not(nullValue())));
		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));
	}

	@Test
	void computeDelta_GumTree_withTimeout() {
		Repository gumtree = new Repository(
			"GumTreeDiff",
			"gumtree",
			"https://github.com/GumTreeDiff/gumtree.git",
			"main"
		);
		Commit v1 = new Commit(gumtree, "2570d34d35daf22e7778c564f19df301433ded21");
		Commit v2 = new Commit(gumtree, "7925aa5e0e7a221e56b5c83de5156034a8ff394f");
		Path v1Clone = CLONES.resolve("v1");
		Path v2Clone = CLONES.resolve("v2");
		CommitBuilder cb1 = new CommitBuilder(v1, v1Clone, new BuildConfig(v1Clone));
		CommitBuilder cb2 = new CommitBuilder(v2, v2Clone, new BuildConfig(v2Clone));
		MaracasOptions opts = MaracasOptions.newDefault();

		ForgeAnalyzer analyzer = new ForgeAnalyzer();
		analyzer.setLibraryBuildTimeout(5);
		assertThrows(BuildException.class, () -> analyzer.computeDelta(cb1, cb2, opts));
	}

	@Test
	void computeImpact_GumTree_withTimeout() throws InterruptedException, ExecutionException {
		Repository gumtree = new Repository(
			"GumTreeDiff",
			"gumtree",
			"https://github.com/GumTreeDiff/gumtree.git",
			"main"
		);
		Repository gumtreeClient = new Repository(
			"SpoonLabs",
			"gumtree-spoon-ast-diff",
			"https://github.com/SpoonLabs/gumtree-spoon-ast-diff.git",
			"master"
		);
		Commit v1 = new Commit(gumtree, "2570d34d35daf22e7778c564f19df301433ded21");
		Commit v2 = new Commit(gumtree, "7925aa5e0e7a221e56b5c83de5156034a8ff394f");
		Path v1Clone = CLONES.resolve("v1");
		Path v2Clone = CLONES.resolve("v2");
		Commit client = new Commit(gumtreeClient, "6533706e98ba0b7be088a2933641aeee3c458c85");
		MaracasOptions opts = MaracasOptions.newDefault();

		ForgeAnalyzer analyzer = new ForgeAnalyzer();
		analyzer.setClientAnalysisTimeout(1);
		Delta delta = analyzer.computeDelta(
			new CommitBuilder(v1, v1Clone, new BuildConfig(v1Clone, Path.of("core"))),
			new CommitBuilder(v2, v2Clone, new BuildConfig(v2Clone, Path.of("core"))),
			opts
		);

		AnalysisResult result = analyzer.computeImpact(
			delta,
			Collections.singletonList(new CommitBuilder(client, CLONES.resolve("client"), Path.of(""))),
			MaracasOptions.newDefault()
		);

		assertThat(result.deltaImpacts(), is(aMapWithSize(1)));

		DeltaImpact impact = result.deltaImpacts().values().stream().findAny().get();
		assertThat(impact.getThrowable(), is(instanceOf(TimeoutException.class)));
		assertThat(impact.getBrokenUses(), is(empty()));
	}
}
