package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.analysis.CommitAnalyzer;
import com.github.maracas.forges.analysis.PullRequestAnalysisResult;
import com.github.maracas.forges.analysis.PullRequestAnalyzer;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.GitHubForge;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
class Usage {
	void readmeUsage1() throws IOException {
		// See https://github-api.kohsuke.org/ to setup the GitHubBuilder
		GitHubForge forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());

		// Option 1: analyzing a pull request
		PullRequestAnalyzer analyzer = new PullRequestAnalyzer(Path.of("/tmp"), forge);
		PullRequest pr = forge.fetchPullRequest("owner", "library", 42);

		PullRequestAnalysisResult result = analyzer.analyze(pr, MaracasOptions.newDefault());
		List<BreakingChange> breakingChanges = result.breakingChanges();
		Set<BrokenUse> brokenUses = result.brokenUses();
	}

	void readmeUsage2() throws IOException {
		// See https://github-api.kohsuke.org/ to setup the GitHubBuilder
		GitHubForge forge = new GitHubForge(GitHubBuilder.fromEnvironment().build());

		// Option 2: analyzing two arbitrary commits
		CommitAnalyzer analyzer = new CommitAnalyzer();
		Commit v1 = forge.fetchCommit("owner", "library", "sha-v1");
		Commit v2 = forge.fetchCommit("owner", "library", "sha-v2");
		Commit client = forge.fetchCommit("owner", "client", "sha");
		CommitBuilder builderV1 = new CommitBuilder(v1, Path.of("clone-v1/"));
		CommitBuilder builderV2 = new CommitBuilder(v2, Path.of("clone-v2/"));
		CommitBuilder builderClient = new CommitBuilder(client, Path.of("clone-client/"));

		AnalysisResult result = analyzer.analyzeCommits(builderV1, builderV2,
			List.of(builderClient), MaracasOptions.newDefault());

		List<BreakingChange> breakingChanges = result.delta().getBreakingChanges();
		Set<BrokenUse> brokenUses = result.allBrokenUses();
	}
}
