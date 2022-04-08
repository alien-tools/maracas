package com.github.maracas.experiments;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubForge;
import com.opencsv.CSVWriter;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.joining;

public class AnalyzeRepositoryHistory {
	private final String owner;
	private final String name;
	private final String fullName;

	private final GitHub gh;
	private final Forge forge;
	private final GHRepository ghRepository;
	private final Repository forgeRepository;
	private final Map<String, Repository> clientRepositories = new HashMap<>();
	private final ClientsManager clientsManager = new ClientsManager();
	private final Properties buildProperties;

	private final Path workingDirectory;
	private final Path clonesPath;
	private final Path reportsPath;
	private final Path cachePath;

	private final Logger logger = LogManager.getLogger(AnalyzeRepositoryHistory.class);

	public AnalyzeRepositoryHistory(
		String owner,
		String name,
		List<String> clients,
		Properties buildProperties,
		Path workingDirectory
	) throws IOException {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;
		this.buildProperties = buildProperties;

		this.workingDirectory = workingDirectory;
		this.clonesPath = workingDirectory.resolve("clones");
		this.reportsPath = workingDirectory.resolve("reports");
		this.cachePath = workingDirectory.resolve("cache");

		this.clonesPath.toFile().mkdirs();
		this.reportsPath.toFile().mkdirs();
		this.cachePath.toFile().mkdirs();

		logger.info("Fetching repositories...");
		this.gh = buildGitHub();
		this.forge = new GitHubForge(gh);
		this.ghRepository = gh.getRepository(fullName);
		this.forgeRepository = forge.fetchRepository(owner, name);

		clients.forEach(c -> {
			var f = c.split("/");
			this.clientRepositories.put(c, forge.fetchRepository(f[0], f[1]));
			try {
				this.clientsManager.addClient(c, gh.getRepository(c));
			} catch (IOException e) {
				logger.error("Cannot resolve client {}: {}", c, e);
			}
		});
	}

	public void analyzePRs(int limit, int threads) throws IOException {
		logger.info("GitHub API: {}/{}, reset at {}",
			gh.getRateLimit().getRemaining(), gh.getRateLimit().getLimit(), gh.getRateLimit().getResetDate());
		logger.info("Fetching PRs for {}", fullName);

		var prs =
			ghRepository.getPullRequests(GHIssueState.ALL)
				.stream()
				.filter(this::affectsJavaFiles)
				.limit(limit)
				.toList();
//			List.of(
//				ghRepository.getPullRequest(4669),
//				ghRepository.getPullRequest(4600),
//				ghRepository.getPullRequest(4202),
//				ghRepository.getPullRequest(3425),
//				ghRepository.getPullRequest(3248),
//				ghRepository.getPullRequest(2173),
//				ghRepository.getPullRequest(2152),
//				ghRepository.getPullRequest(1090),
//				ghRepository.getPullRequest(444),
//				ghRepository.getPullRequest(21)
//			);

		var analyzer = new ForgeAnalyzer();
		var executor = Executors.newFixedThreadPool(threads);
		var csvPath = workingDirectory.resolve("%s-%s.csv".formatted(owner, name));
		var csvFile = new File(csvPath.toAbsolutePath().toString());
		logger.info("Analyzing {}: {} PRs found", fullName, prs.size());

		try (var csvWriter = new CSVWriter(new FileWriter(csvFile))) {
			csvWriter.writeNext((
				"owner,name,number,state,title,mergeable,created,merged,closed,url,commits," +
					"baseRef,base,headRef,head,author,labels,additions,deletions,changedFiles,changedJavaFiles," +
					"comments,bcs,checkedClients,brokenClients,brokenUses,report,message,errors").split(","));
			csvWriter.flush();

			var tasks = new ArrayList<Callable<Object>>(prs.size());
			for (var pr : prs) {
				tasks.add(Executors.callable(() -> {
					var prId = "%s#%d".formatted(fullName, pr.getNumber());
					logger.info("Analyzing {} [{}]: {} [{}]", prId, pr.getState(), pr.getTitle(), pr.getClosedAt());

					var prClone = clonesPath.resolve(String.valueOf(pr.getNumber()));
					var forgePr = forge.fetchPullRequest(forgeRepository, pr.getNumber());
					var buildV1 = new CommitBuilder(forgePr.prBase(), prClone.resolve("base-" + forgePr.prBase().sha()));
					var buildV2 = new CommitBuilder(forgePr.head(), prClone.resolve("head-" + forgePr.head().sha()));
					buildV1.setBuildProperties(buildProperties);
					buildV2.setBuildProperties(buildProperties);

					var reportFile = reportsPath.resolve(pr.getNumber() + "-report.json");

					try {
						// Compute delta
						Delta delta = analyzer.computeDelta(buildV1, buildV2, MaracasOptions.newDefault());
						int bcs = delta.getBreakingChanges().size();

						// We got something, look at the clients
						if (bcs > 0) {
							logger.info("Retrieving potentially impacted clients");
							var clientCommits = clientsManager.clientsAtDate(pr.getClosedAt());
							var buildClients = buildersFor(clientCommits);

							logger.info("Potentially impacted clients [{}/{}]:",
								clientCommits.size(), clientRepositories.size());
							clientCommits.forEach((name, commit) -> {
								try {
									logger.info("\t{}@{}: {} [{}]", name, commit.getSHA1(),
										commit.getCommitShortInfo().getMessage().split("\n")[0],
										commit.getCommitDate());
								} catch (IOException e) {
									e.printStackTrace();
								}
							});

							var result = analyzer.computeImpact(delta, buildClients);
							int checkedClients = result.deltaImpacts().size();
							int brokenClients = result.brokenClients().size();
							int allBrokenUses = result.allBrokenUses().size();
							logger.info("{}: found {} BCs, {}/{} broken clients, and {} broken uses",
								prId, bcs, brokenClients, checkedClients, allBrokenUses);
							logger.info("Serializing report to {}", reportFile);
							Files.write(reportFile, result.toJson().getBytes());

							String clientErrors = result.deltaImpacts().values().stream().map(
								i -> i.getThrowable() != null ? i.getThrowable().getMessage() : ""
							).collect(joining("+"));

							writeLine(csvWriter, pr, bcs, checkedClients, brokenClients, allBrokenUses,
								reportFile.toAbsolutePath().toString(), "ok", clientErrors);
						}
						// We got nothing, write report
						else {
							logger.info("{}: No breaking change", prId);
							logger.info("Serializing report to {}", reportFile);
							Files.write(reportFile, AnalysisResult.noImpact(delta, Collections.emptyList()).toJson().getBytes());

							writeLine(csvWriter, pr, bcs, -1, -1, -1,
								reportFile.toAbsolutePath().toString(), "no-bc", "");
						}
					} catch (Exception e) {
						logger.error("Error analyzing {}: {}", prId, e);
						writeLine(csvWriter, pr, -1, -1, -1, -1,
							"-1", e.getMessage(), "-1");
					}
				}));
			}

			executor.invokeAll(tasks);
			logger.info("Analysis is over!");

			executor.shutdown();
			try {
				if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private synchronized void writeLine(CSVWriter writer, GHPullRequest pr, int bcs, int checkedClients,
	                                    int brokenClients, int allBrokenUses, String reportFile, String message, String errors) {
		try {
			var line = new String[]{
				owner, name, "" + pr.getNumber(), "" + pr.getState(), pr.getTitle(),
				"" + pr.getMergeable(), "" + pr.getCreatedAt(), "" + pr.getMergedAt(), "" + pr.getClosedAt(),
				"" + pr.getHtmlUrl(), "" + pr.getCommits(), pr.getBase().getRef(), pr.getBase().getSha(),
				pr.getHead().getRef(), pr.getHead().getSha(), pr.getUser().getLogin(),
				pr.getLabels().stream().map(GHLabel::getName).collect(joining("+")), "" + pr.getAdditions(),
				"" + pr.getDeletions(), "" + pr.getChangedFiles(),
				"" + pr.listFiles().toList().stream().filter(f -> f.getFilename().endsWith(".java")).count(),
				"" + pr.getCommentsCount(), "" + bcs, "" + checkedClients, "" + brokenClients, "" + allBrokenUses,
				reportFile, message, errors
			};

			writer.writeNext(line);
			writer.flush();
		} catch (IOException e) {
			logger.error("Error writing CSV", e);
		}
	}

	private boolean affectsJavaFiles(GHPullRequest pr) {
		try {
			return pr.listFiles().toList().stream().anyMatch(f -> f.getFilename().endsWith(".java"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<CommitBuilder> buildersFor(Map<String, GHCommit> clientCommits) {
		return clientCommits.keySet().stream()
			.map(clientName -> {
				var f = clientName.split("/");
				var clientRepo = clientRepositories.get(clientName);
				var commit = clientCommits.get(clientName);

				if (clientRepo == null)
					logger.warn("Skipping {}; was the repository renamed?", clientName);

				var forgeCommit = forge.fetchCommit(clientRepo, commit.getSHA1());
				var wd =
					clonesPath
						.resolve("clients")
						.resolve(f[0])
						.resolve(f[1])
						.resolve(commit.getSHA1());
				var builder = new CommitBuilder(forgeCommit, wd);

				// FIXME: ;)
				if ("dspot".equals(f[1]))
					builder.setSources(Paths.get("dspot/src/main/java"));
				if ("nopol".equals(f[1]))
					builder.setSources(Paths.get("nopol/src/main/java"));

				return builder;
			})
			.toList();
	}

	/**
	 * Builds a GitHub client with built-in caching to temper rate-limited API calls
	 */
	private GitHub buildGitHub() throws IOException {
		var builder = new OkHttpClient().newBuilder();

		var cacheDir = cachePath.toFile();
		cacheDir.mkdirs();
		var cache = new Cache(cacheDir, 100 * 1024L * 1024L);
		builder.cache(cache);

		return
			GitHubBuilder.fromEnvironment()
				.withConnector(new OkHttpGitHubConnector(builder.build()))
				.build();
	}

	public static void main(String[] args) throws Exception {
		var spoonProps = new Properties();
		spoonProps.setProperty("maven.test.skip", "true");
		spoonProps.setProperty("skipDepClean", "true");
		spoonProps.setProperty("assembly.skipAssembly", "true");
		spoonProps.setProperty("jacoco.skip", "true");
		spoonProps.setProperty("mdep.skip", "true");
		spoonProps.setProperty("checkstyle.skip", "true");
		spoonProps.setProperty("enforcer.skip", "true");

		var spoonClients = List.of(
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
			"KTH/xPerturb",
			"SpoonLabs/metamutator",
			"SpoonLabs/spooet",
			"KTH/spork"
		);

		new AnalyzeRepositoryHistory("INRIA", "spoon", spoonClients, spoonProps, Paths.get("./data"))
			.analyzePRs(2000, 4);
	}
}
