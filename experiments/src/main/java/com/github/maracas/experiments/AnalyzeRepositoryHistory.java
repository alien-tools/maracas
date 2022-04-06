package com.github.maracas.experiments;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class AnalyzeRepositoryHistory {
	private static final Path CLONE_PATH = Paths.get("clones");
	private final String owner;
	private final String name;
	private final String fullName;

	private final GitHub gh;
	private final Forge forge;

	private final Logger logger = LogManager.getLogger(AnalyzeRepositoryHistory.class);

	public AnalyzeRepositoryHistory(String owner, String name) throws IOException {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;

		this.gh = GitHubBuilder.fromEnvironment().build();
		this.forge = new GitHubForge(gh);
	}

	public void analyze() throws Exception {
		logger.info("Fetching PRs for {}", fullName);
		var ghRepo = gh.getRepository(fullName);
		var forgeRepo = forge.fetchRepository(owner, name);
		var prs =
			ghRepo.getPullRequests(GHIssueState.ALL)
				.stream()
				.filter(this::affectsJavaFiles)
				.limit(5)
				.toList();

		logger.info("Fetching releases for {}", fullName);
		var releases = ghRepo.listReleases().toList();

		logger.info("Analyzing {}: {} PRs and {} releases", fullName, prs.size(), releases.size());
		ForgeAnalyzer analyzer = new ForgeAnalyzer();
		int i = 0;
		for (var pr : prs) {
			i++;
			logger.info("[{}/{}] Analyzing {}#{} [{}]: {}", i, prs.size(), fullName, pr.getNumber(), pr.getState(), pr.getTitle());

			Path wd = CLONE_PATH.resolve(String.valueOf(pr.getNumber()));
			var forgePr = forge.fetchPullRequest(forgeRepo, pr.getNumber());
			var buildV1 = new CommitBuilder(forgePr.prBase(), wd.resolve("base"));
			var buildV2 = new CommitBuilder(forgePr.head(), wd.resolve("head"));
			Properties props = new Properties();
			props.setProperty("maven.test.skip", "true");
			props.setProperty("skipDepClean", "true");
			props.setProperty("assembly.skipAssembly", "true");
			props.setProperty("jacoco.skip", "true");
			props.setProperty("mdep.skip", "true");
			props.setProperty("checkstyle.skip", "true");
			buildV1.setBuildProperties(props);
			buildV2.setBuildProperties(props);
			var buildClients = fetchClients();

			AnalysisResult result = analyzer.analyzeCommits(buildV1, buildV2, buildClients, MaracasOptions.newDefault());
			logger.info(result);
		}
	}

	private boolean affectsJavaFiles(GHPullRequest pr) {
		try {
			return pr.listFiles().toList().stream().anyMatch(f -> f.getFilename().endsWith(".java"));
		} catch (IOException e) {
			return false;
		}
	}

	public List<CommitBuilder> fetchClients() {
		return List.of(
			"SpoonLabs/flacoco",
			"SpoonLabs/coming",
			"SpoonLabs/astor",
			"SpoonLabs/npefix",
			"SpoonLabs/nopol",
			"STAMP-project/AssertFixer",
			"Spirals-Team/casper",
			"SpoonLabs/CoCoSpoon",
			"STAMP-project/dspot",
			"SpoonLabs/gumtree-spoon-ast-diff",
			"Spirals-Team/jPerturb",
			"SpoonLabs/metamutator",
			"SpoonLabs/spooet",
			"KTH/spork"
		).stream().map(c -> {
			String[] f = c.split("/");
			var clientRepo = forge.fetchRepository(f[0], f[1]);
			Path wd =
				CLONE_PATH
				.resolve("clients")
				.resolve(f[1])
				.resolve("HEAD");
			var builder = new CommitBuilder(forge.fetchCommit(clientRepo, "HEAD"), wd);

			// FIXME ;)
			if ("dspot".equals(f[1]))
				builder.setSources(Paths.get("dspot/src/main/java"));
			if ("nopol".equals(f[1]))
				builder.setSources(Paths.get("nopol/src/main/java"));

			return builder;
		}).toList();
	}

	public static void main(String[] args) {
		try {
			new AnalyzeRepositoryHistory("INRIA", "spoon").analyze();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
