package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.ClientFetcher;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.Package;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuilderFactory;
import com.github.maracas.forges.build.CommitBuilderFactory;
import com.github.maracas.forges.clone.ClonerFactory;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.ForgeBreakingChange;
import com.github.maracas.forges.report.ForgeBrokenUse;
import com.github.maracas.forges.report.PackageReport;
import com.github.maracas.forges.report.PullRequestReport;
import japicmp.model.JApiCompatibilityChange;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GitHubBuilder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PullRequestAnalyzerTest {
	final Path workingDirectory = Path.of("./clones");
	Forge forge;
	Repository fixture;
	PullRequestAnalyzer prAnalyzer;

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(workingDirectory.toFile());

		forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());
		fixture = forge.fetchRepository("alien-tools", "repository-fixture");
		Package moduleA = new Package("com.github.alien-tools:module-a", Path.of("module-a"));
		Package nestedB = new Package("com.github.alien-tools:nested-b", Path.of("module-c/nested-b"));
		Commit clientA = forge.fetchCommit("alien-tools", "client-fixture-a", "HEAD");
		Commit clientB = forge.fetchCommit("alien-tools", "client-fixture-b", "HEAD");

		ClientFetcher clientFetcher = mock(ClientFetcher.class);
		when(clientFetcher.fetchClients(eq(fixture), eq(moduleA), anyInt(), anyInt())).thenReturn(List.of(clientA));
		when(clientFetcher.fetchClients(eq(fixture), eq(nestedB), anyInt(), anyInt())).thenReturn(List.of(clientB));

		CommitBuilderFactory commitBuilderFactory = new CommitBuilderFactory(new ClonerFactory(), new BuilderFactory());
		CommitAnalyzer commitAnalyzer = new ParallelCommitAnalyzer();
		prAnalyzer = new PullRequestAnalyzer(workingDirectory, commitBuilderFactory, clientFetcher, commitAnalyzer);
	}

	@Test
	void analyzePullRequest_fixture_normal() {
		PullRequest pr = forge.fetchPullRequest(fixture, 1);
		PullRequestReport report = prAnalyzer.analyze(pr, MaracasOptions.newDefault());

		assertThat(report.error(), is(nullValue()));
		assertThat(report.pr().number(), is(equalTo(1)));
		assertThat(report.pr().baseBranch(), is(equalTo("main")));
		assertThat(report.pr().headBranch(), is(equalTo("pr-on-modules")));
		assertThat(report.pr().changedFiles(), hasSize(2));
		assertThat(report.pr().changedJavaFiles(), hasSize(2));

		assertThat(report.packageReports(), hasSize(2));

		PackageReport moduleA = report.packageReports().get(0);
		assertThat(moduleA.error(), is(nullValue()));
		assertThat(moduleA.delta(), is(not(nullValue())));
		assertThat(moduleA.delta().breakingChanges(), hasSize(1));
		assertThat(moduleA.clientsImpact(), hasSize(1));

		ForgeBreakingChange fbcA = moduleA.delta().breakingChanges().get(0);
		assertThat(fbcA.path(), is(equalTo("module-a/src/main/java/modulea/A.java")));
		assertThat(fbcA.startLine(), is(equalTo(4)));
		assertThat(fbcA.endLine(), is(equalTo(6)));
		assertThat(fbcA.fileUrl(), is(equalTo("https://github.com/alien-tools/repository-fixture/blob/main/module-a/src/main/java/modulea/A.java#L4-L6")));
		assertThat(fbcA.diffUrl(), is(equalTo("https://github.com/alien-tools/repository-fixture/pull/1/files#diff-fbb607db0239487679342dbde80c69e4105cd269a5fa594c3d90d3baf91a8e6eL4-L6")));
		assertThat(fbcA.breakingChange().getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(fbcA.breakingChange().getReference().toString(), is(equalTo("a()")));

		ClientImpact clientA = moduleA.clientsImpact().get(0);
		assertThat(clientA.client().repository().owner(), is(equalTo("alien-tools")));
		assertThat(clientA.client().repository().name(), is(equalTo("client-fixture-a")));
		assertThat(clientA.error(), is(nullValue()));
		assertThat(clientA.brokenUses(), hasSize(1));
		ForgeBrokenUse fbuA = clientA.brokenUses().get(0);
		assertThat(fbuA.path(), is(equalTo("src/main/java/clienta/ClientA.java")));
		assertThat(fbuA.startLine(), is(equalTo(9)));
		assertThat(fbuA.endLine(), is(equalTo(9)));
		assertThat(fbuA.url(), is(equalTo("https://github.com/alien-tools/client-fixture-a/blob/main/src/main/java/clienta/ClientA.java#L9-L9")));

		PackageReport moduleB = report.packageReports().get(1);
		assertThat(moduleB.error(), is(nullValue()));
		assertThat(moduleB.delta(), is(not(nullValue())));
		assertThat(moduleB.delta().breakingChanges(), hasSize(1));
		assertThat(moduleB.clientsImpact(), hasSize(1));

		ForgeBreakingChange fbcB = moduleB.delta().breakingChanges().get(0);
		assertThat(fbcB.path(), is(equalTo("module-c/nested-b/src/main/java/nestedb/NestedB.java")));
		assertThat(fbcB.startLine(), is(equalTo(4)));
		assertThat(fbcB.endLine(), is(equalTo(6)));
		assertThat(fbcB.fileUrl(), is(equalTo("https://github.com/alien-tools/repository-fixture/blob/main/module-c/nested-b/src/main/java/nestedb/NestedB.java#L4-L6")));
		assertThat(fbcB.diffUrl(), is(equalTo("https://github.com/alien-tools/repository-fixture/pull/1/files#diff-fb67a83f3cdd140afada868502de4908e1c98fd6fefc03c05f66139278dda700L4-L6")));
		assertThat(fbcB.breakingChange().getChange(), is(equalTo(JApiCompatibilityChange.METHOD_REMOVED)));
		assertThat(fbcB.breakingChange().getReference().toString(), is(equalTo("nestedB()")));

		ClientImpact clientB = moduleB.clientsImpact().get(0);
		assertThat(clientB.client().repository().owner(), is(equalTo("alien-tools")));
		assertThat(clientB.client().repository().name(), is(equalTo("client-fixture-b")));
		assertThat(clientB.error(), is(nullValue()));
		assertThat(clientB.brokenUses(), hasSize(1));
		ForgeBrokenUse fbuB = clientB.brokenUses().get(0);
		assertThat(fbuB.path(), is(equalTo("src/main/java/clientb/ClientB.java")));
		assertThat(fbuB.startLine(), is(equalTo(14)));
		assertThat(fbuB.endLine(), is(equalTo(14)));
		assertThat(fbuB.url(), is(equalTo("https://github.com/alien-tools/client-fixture-b/blob/main/src/main/java/clientb/ClientB.java#L14-L14")));
	}

	@Test
	void analyzePullRequest_fixture_noJavaFile() {
		PullRequest pr = forge.fetchPullRequest(fixture, 2);
		PullRequestReport report = prAnalyzer.analyze(pr, MaracasOptions.newDefault());

		assertThat(report.error(), is(nullValue()));
		assertThat(report.pr().number(), is(equalTo(2)));
		assertThat(report.pr().baseBranch(), is(equalTo("main")));
		assertThat(report.pr().headBranch(), is(equalTo("pr-on-readme")));
		assertThat(report.pr().changedFiles(), hasSize(1));
		assertThat(report.pr().changedJavaFiles(), hasSize(0));

		assertThat(report.packageReports(), is(empty()));
	}

	@Test
	void analyzePullRequest_fixture_pr_on_another_branch() {
		PullRequest pr = forge.fetchPullRequest(fixture, 3);
		PullRequestReport report = prAnalyzer.analyze(pr, MaracasOptions.newDefault());

		assertThat(report.error(), is(nullValue()));
		assertThat(report.pr().number(), is(equalTo(3)));
		assertThat(report.pr().baseBranch(), is(equalTo("pr-on-modules")));
		assertThat(report.pr().headBranch(), is(equalTo("pr-on-a-branch")));
		assertThat(report.pr().changedFiles(), hasSize(1));
		assertThat(report.pr().changedJavaFiles(), hasSize(1));

		assertThat(report.packageReports(), hasSize(1));
		assertThat(report.packageReports().get(0).delta().breakingChanges(), hasSize(1));
		assertThat(report.packageReports().get(0).clientsImpact(), hasSize(1));
		assertThat(report.packageReports().get(0).clientsImpact().get(0).brokenUses(), hasSize(0));
	}
}