package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.BuilderFactory;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.build.CommitBuilderFactory;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.ClonerFactory;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.forges.report.ClientImpact;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelCommitAnalyzerTest {
	final Path workingDirectory = Path.of("./clones");
	Forge forge;
	ParallelCommitAnalyzer commitAnalyzer;
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
		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
	}

	@Test
	void computeDelta_maracas_withBuildTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = commitBuilderFactory.createLibraryBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = commitBuilderFactory.createLibraryBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		opts.setBuildTimeoutSeconds(1);
		Exception thrown = assertThrows(BuildException.class, () -> commitAnalyzer.computeDelta(cb1, cb2, opts));
		assertThat(thrown.getMessage(), containsString("timed out"));
	}

	@Test
	void computeDelta_maracas_withCloneTimeout() {
		Commit v1 = forge.fetchCommit("alien-tools", "maracas", "b7e1cd");
		Commit v2 = forge.fetchCommit("alien-tools", "maracas", "69a666");
		CommitBuilder cb1 = commitBuilderFactory.createLibraryBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("core")));
		CommitBuilder cb2 = commitBuilderFactory.createLibraryBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("core")));
		MaracasOptions opts = MaracasOptions.newDefault();

		opts.setCloneTimeoutSeconds(1);
		Exception thrown = assertThrows(CloneException.class, () -> commitAnalyzer.computeDelta(cb1, cb2, opts));
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

		Delta delta = commitAnalyzer.computeDelta(
			commitBuilderFactory.createLibraryBuilder(v1, workingDirectory.resolve("v1"), new BuildConfig(Path.of("module-a"))),
			commitBuilderFactory.createLibraryBuilder(v2, workingDirectory.resolve("v2"), new BuildConfig(Path.of("module-a"))),
			opts
		);

		opts.setCloneTimeoutSeconds(2);
		List<ClientImpact> result = commitAnalyzer.computeImpact(
			delta,
			List.of(
				commitBuilderFactory.createClientBuilder(client1, workingDirectory.resolve("client1"), BuildConfig.newDefault()),
				commitBuilderFactory.createClientBuilder(client2, workingDirectory.resolve("client2"), BuildConfig.newDefault()),
				commitBuilderFactory.createClientBuilder(client3, workingDirectory.resolve("client3"), BuildConfig.newDefault())
			),
			opts
		);

		assertThat(result, hasSize(3));
		assertTrue(result.stream().anyMatch(impact -> impact.error() != null && impact.error().contains("timed out")));
	}
}