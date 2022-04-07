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
import org.checkerframework.checker.units.qual.C;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AnalyzeRepositoryHistory {
	private static final Path CSV_PATH = Paths.get("data");
	private static final Path CLONE_PATH = Paths.get("data/clones");
	private static final Path REPORTS_PATH = Paths.get("data/reports");
	private static final Path GH_CACHE = Paths.get("cache");
	private final String owner;
	private final String name;
	private final String fullName;

	private final GitHub gh;
	private final Forge forge;
	private final GHRepository ghRepository;
	private final Repository forgeRepository;
	private final Map<String, Repository> clientRepositories = new HashMap<>();
	private final ClientsManager clientsManager = new ClientsManager();

	private final Logger logger = LogManager.getLogger(AnalyzeRepositoryHistory.class);

	public AnalyzeRepositoryHistory(String owner, String name, List<String> clients) throws IOException {
		CSV_PATH.toFile().mkdirs();
		CLONE_PATH.toFile().mkdirs();
		REPORTS_PATH.toFile().mkdirs();

		logger.info("Fetching all repos");
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;

		this.gh = GitHubBuilder.fromEnvironment()
			.withConnector(new OkHttpGitHubConnector(createClient()))
			.build();
		this.forge = new GitHubForge(gh);
		this.ghRepository = gh.getRepository(fullName);
		this.forgeRepository = forge.fetchRepository(owner, name);

		clients.forEach(c -> {
			String[] f = c.split("/");
			this.clientRepositories.put(c, forge.fetchRepository(f[0], f[1]));
			try {
				clientsManager.addClient(c, gh.getRepository(c));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private OkHttpClient createClient() {
		OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

		File cacheDir = GH_CACHE.toFile();
		cacheDir.mkdirs();
		Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);

		builder.cache(cache);

		return builder.build();
	}

	public void analyze() throws Exception {
		logger.info("GitHub API: {}/{}, reset at {}", gh.getRateLimit().getRemaining(), gh.getRateLimit().getLimit(), gh.getRateLimit().getResetDate());
		logger.info("Fetching PRs for {}", fullName);
		var prs =
			ghRepository.getPullRequests(GHIssueState.ALL)
				.stream()
				.filter(this::affectsJavaFiles)
				.limit(2000)
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

		logger.info("Fetching releases for {}", fullName);
		var releases = ghRepository.listReleases().toList();

		logger.info("Analyzing {}: {} PRs and {} releases", fullName, prs.size(), releases.size());
		ForgeAnalyzer analyzer = new ForgeAnalyzer();
		var executor = Executors.newFixedThreadPool(4);
		File csvOutputFile = new File(CSV_PATH.resolve("%s-%s.csv".formatted(owner, name)).toAbsolutePath().toString());
		try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csvOutputFile))) {
			csvWriter.writeNext(
				"owner,name,number,state,title,mergeable,created,merged,closed,url,commits,baseRef,base,headRef,head,author,labels,additions,deletions,changedFiles,changedJavaFiles,comments,bcs,checkedClients,brokenClients,brokenUses,report,message,errors"
					.split(","));
			csvWriter.flush();
			var tasks = new ArrayList<Callable<Object>>(prs.size());
			for (var pr : prs) {
				tasks.add(Executors.callable(() -> {
					logger.info("Analyzing {}#{} [{}]: {} [{}]", fullName, pr.getNumber(), pr.getState(), pr.getTitle(), pr.getClosedAt());

					Path wd = CLONE_PATH.resolve(String.valueOf(pr.getNumber()));
					var forgePr = forge.fetchPullRequest(forgeRepository, pr.getNumber());
					var buildV1 = new CommitBuilder(forgePr.prBase(), wd.resolve("base"));
					var buildV2 = new CommitBuilder(forgePr.head(), wd.resolve("head"));
					Properties props = new Properties();
					props.setProperty("maven.test.skip", "true");
					props.setProperty("skipDepClean", "true");
					props.setProperty("assembly.skipAssembly", "true");
					props.setProperty("jacoco.skip", "true");
					props.setProperty("mdep.skip", "true");
					props.setProperty("checkstyle.skip", "true");
					props.setProperty("enforcer.skip", "true");
					buildV1.setBuildProperties(props);
					buildV2.setBuildProperties(props);

					Path reportFile = REPORTS_PATH.resolve(pr.getNumber() + "-report.json");

					// Compute the delta first, then, if it breaks, check clients
					try {
						Delta delta = analyzer.computeDelta(buildV1, buildV2, MaracasOptions.newDefault());
						if (delta.getBreakingChanges().isEmpty()) {
							int bcs = delta.getBreakingChanges().size();
							logger.info("{}/{}#{}: No breaking change", owner, name, pr.getNumber());
							logger.info("Serializing report to {}", reportFile);
							Files.write(reportFile, AnalysisResult.noImpact(delta, Collections.emptyList()).toJson().getBytes());

							synchronized (this) {
								var line = new String[]{owner, name, "" + pr.getNumber(), "" + pr.getState(), pr.getTitle(), "" + pr.getMergeable(), "" + pr.getCreatedAt(), "" + pr.getMergedAt(), "" + pr.getClosedAt(), "" + pr.getHtmlUrl(), "" + pr.getCommits(),
									pr.getBase().getRef(), pr.getBase().getSha(), pr.getHead().getRef(), pr.getHead().getSha(), pr.getUser().getLogin(),
									pr.getLabels().stream().map(GHLabel::getName).collect(Collectors.joining("+")), "" + pr.getAdditions(), "" + pr.getDeletions(), "" + pr.getChangedFiles(),
									"" + pr.listFiles().toList().stream().filter(f -> f.getFilename().endsWith(".java")).count(), "" + pr.getCommentsCount(),
									"" + bcs, "-1", "-1", "-1", "" + reportFile, "-1",
									"-1"};
								csvWriter.writeNext(line);
								csvWriter.flush();
							}
						} else {
							logger.info("Retrieving potentially impacted clients");
							var clientCommits = clientsManager.clientsAtDate(pr.getClosedAt());
							logger.info("Creating builders");
							var buildClients = buildersFor(clientCommits);

							logger.info("Potentially impacted clients [{}/{}]:", clientCommits.size(), clientRepositories.size());
							clientCommits.keySet().forEach(name -> {
								GHCommit commit = clientCommits.get(name);
								try {
									logger.info("\t{}@{}: {} [{}]", name, commit.getSHA1(), commit.getCommitShortInfo().getMessage().split("\n")[0], commit.getCommitDate());
								} catch (IOException e) {
									e.printStackTrace();
								}
							});

							AnalysisResult result = analyzer.computeImpact(delta, buildClients);
							int bcs = result.delta().getBreakingChanges().size();
							int checkedClients = result.deltaImpacts().size();
							int brokenClients = (int) result.deltaImpacts().values().stream().filter(i -> !i.getBrokenUses().isEmpty()).count();
							int allBrokenUses = result.allBrokenUses().size();
							logger.info("{}/{}#{}: found {} BCs, {}/{} broken clients, and {} broken uses", owner, name, pr.getNumber(), bcs, brokenClients, checkedClients, allBrokenUses);
							logger.info("Serializing report to {}", reportFile);
							Files.write(reportFile, result.toJson().getBytes());

							synchronized (this) {
								var line = new String[]{owner, name, "" + pr.getNumber(), "" + pr.getState(), pr.getTitle(), "" + pr.getMergeable(), "" + pr.getCreatedAt(), "" + pr.getMergedAt(), "" + pr.getClosedAt(), "" + pr.getHtmlUrl(), "" + pr.getCommits(),
									pr.getBase().getRef(), pr.getBase().getSha(), pr.getHead().getRef(), pr.getHead().getSha(), pr.getUser().getLogin(),
									pr.getLabels().stream().map(GHLabel::getName).collect(Collectors.joining("+")), "" + pr.getAdditions(), "" + pr.getDeletions(), "" + pr.getChangedFiles(),
									"" + pr.listFiles().toList().stream().filter(f -> f.getFilename().endsWith(".java")).count(), "" + pr.getCommentsCount(),
									"" + bcs, "" + checkedClients, "" + brokenClients, "" + allBrokenUses, "" + reportFile, "",
									result.deltaImpacts().values().stream().map(i -> i.getThrowable() != null ? i.getThrowable().getMessage() : "").collect(Collectors.joining("+"))};
								csvWriter.writeNext(line);
								csvWriter.flush();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.info("Error analyzing " + pr);
						synchronized (this) {
							try {
								var line = new String[]{owner, name, "" + pr.getNumber(), "" + pr.getState(), pr.getTitle(), "" + pr.getMergeable(), "" + pr.getCreatedAt(), "" + pr.getMergedAt(), "" + pr.getClosedAt(), "" + pr.getHtmlUrl(), "" + pr.getCommits(),
									pr.getBase().getRef(), pr.getBase().getSha(), pr.getHead().getRef(), pr.getHead().getSha(), pr.getUser().getLogin(),
									pr.getLabels().stream().map(GHLabel::getName).collect(Collectors.joining("+")), "" + pr.getAdditions(), "" + pr.getDeletions(), "" + pr.getChangedFiles(),
									"" + pr.listFiles().toList().stream().filter(f -> f.getFilename().endsWith(".java")).count(), "" + pr.getCommentsCount(),
									"-1", "-1", "-1", "-1", "-1", e.getMessage(),
									"-1"};
								csvWriter.writeNext(line);
								csvWriter.flush();
							} catch (IOException ee) {
								ee.printStackTrace();
							}
						}
					}
				}));
			}

			executor.invokeAll(tasks);
		} catch (IOException e) {
			e.printStackTrace();
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
				String[] f = clientName.split("/");
				var clientRepo = clientRepositories.get(clientName);
				var commit = clientCommits.get(clientName);

				if (clientRepo == null)
					logger.warn("Skipping {}; was the repository renamed?", clientName);

				var forgeCommit = forge.fetchCommit(clientRepo, commit.getSHA1());
				var wd =
					CLONE_PATH
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

	public static void main(String[] args) {
		try {
			new AnalyzeRepositoryHistory(
				"INRIA", "spoon",
				List.of(
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
				)
			).analyze();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
