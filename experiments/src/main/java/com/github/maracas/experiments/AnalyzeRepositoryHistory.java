package com.github.maracas.experiments;

import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.analysis.CommitAnalyzer;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.github.GitHubForge;
import com.opencsv.CSVWriter;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHDirection;
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
	private final Path module;

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
		Path module,
		Path workingDirectory
	) throws IOException {
		this.owner = owner;
		this.name = name;
		this.fullName = owner + "/" + name;
		this.buildProperties = buildProperties;
		this.module = module;

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

		var iterator =
			ghRepository.queryPullRequests()
				.state(GHIssueState.ALL)
				.direction(GHDirection.DESC)
				.list()
				.withPageSize(100)
				.iterator();

		while (iterator.hasNext()) {
			var prs = iterator.nextPage().stream().filter(this::affectsJavaFiles).toList();
			var analyzer = new CommitAnalyzer(new Maracas());
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
						var cloneV1 = prClone.resolve("base-" + forgePr.mergeBase().sha());
						var cloneV2 = prClone.resolve("head-" + forgePr.head().sha());
						var configV1 = new BuildConfig(module);
						var configV2 = new BuildConfig(module);
						buildProperties.forEach((k, v) -> {
							configV1.setProperty(k.toString(), v.toString());
							configV2.setProperty(k.toString(), v.toString());
						});
						var buildV1 = new CommitBuilder(forgePr.mergeBase(), cloneV1, configV1);
						var buildV2 = new CommitBuilder(forgePr.head(), cloneV2, configV2);

						var reportFile = reportsPath.resolve(pr.getNumber() + "-report.json");

						try {
							// Compute delta
							var opts = MaracasOptions.newDefault();
							Delta delta = analyzer.computeDelta(buildV1, buildV2, opts);
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

								var result = analyzer.computeImpact(delta, buildClients, opts);
								int checkedClients = result.deltaImpacts().size();
								int brokenClients = result.brokenClients().size();
								int allBrokenUses = result.allBrokenUses().size();
								logger.info("{}: found {} BCs, {}/{} broken clients, and {} broken uses",
									prId, bcs, brokenClients, checkedClients, allBrokenUses);
								logger.info("Serializing report to {}", reportFile);
								Files.write(reportFile, result.toJson().getBytes());

								String clientErrors = result.deltaImpacts().values().stream().map(
									i ->
										i.throwable() != null
											? i.toString().getClass().toString() + ":" + i.throwable().getMessage()
											: "none"
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
				var builder = new CommitBuilder(forgeCommit, wd, BuildConfig.newDefault());

				// FIXME: ;)
				if ("dspot".equals(f[1]))
					builder = new CommitBuilder(forgeCommit, wd, new BuildConfig(Path.of("dspot")));
				if ("nopol".equals(f[1]))
					builder = new CommitBuilder(forgeCommit, wd, new BuildConfig(Path.of("nopol")));

				return builder;
			})
			.toList();
	}

	/**
	 * Builds a GitHub client with built-in caching to temper rate-limited API calls
	 */
	private GitHub buildGitHub() throws IOException {
		var builder = new OkHttpClient().newBuilder();
		builder.addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				logger.info("Sending request {}", chain.request().url());
				return chain.proceed(chain.request());
			}
		});

		var cacheDir = cachePath.toFile();
		cacheDir.mkdirs();
		var cache = new Cache(cacheDir, 100 * 1024L * 1024L);
		builder.cache(cache);

		return
			GitHubBuilder.fromEnvironment()
				.withConnector(new OkHttpGitHubConnector(builder.build()))
				.build();
	}

	public static void spoon() throws IOException {
		Path spoonModule = Path.of("");
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

		new AnalyzeRepositoryHistory(
			"INRIA", "spoon", spoonClients, spoonProps, spoonModule, Path.of("./data"))
			.analyzePRs(2000, 4);
	}

	public static void javaparser() throws IOException {
		var jpModule = Path.of("javaparser-core");
		var jpProps = new Properties();
		jpProps.setProperty("maven.test.skip", "true");

		var jpClients = List.of(
			"OpenAPITools/openapi-generator",
			"quarkusio/quarkus",
			"facebookarchive/nuclide",
			"mybatis/generator",
			"lettuce-io/lettuce-core",
			"kiegroup/drools",
			"apache/camel",
			"javaparser/javaparser",
			"Col-E/Recaf",
			"JCTools/JCTools",
			"fabric8io/kubernetes-client",
			"Azure/azure-sdk-for-java",
			"umlet/umlet",
			"zstackio/zstack",
			"artur-shaik/vim-javacomplete2",
			"infinispan/infinispan",
			"tech-srl/code2vec",
			"javalite/javalite",
			"actframework/actframework",
			"junkdog/artemis-odb",
			"apache/isis",
			"structr/structr",
			"spawpaw/mybatis-generator-gui-extension",
			"tensorflow/java",
			"youngyangyang04/PowerVim",
			"tech-srl/code2seq",
			"moditect/moditect",
			"kiegroup/kogito-runtimes",
			"sdaschner/jaxrs-analyzer",
			"didi/super-jacoco",
			"kiegroup/droolsjbpm-build-bootstrap",
			"VueGWT/vue-gwt",
			"xgdsmileboy/SimFix",
			"helios-decompiler/standalone-app",
			"kklisura/chrome-devtools-java-client",
			"benas/jql",
			"abstracta/jmeter-java-dsl",
			"hussien89aa/MigrationMiner",
			"dodie/scott",
			"ecmnet/MAVGCL",
			"gwt-test-utils/gwt-test-utils",
			"RepreZen/KaiZen-OpenApi-Parser",
			"liuzhengyang/lets-hotfix",
			"davidmoten/state-machine",
			"kiegroup/droolsjbpm-knowledge",
			"Col-E/JRemapper",
			"danielzuegner/code-transformer",
			"STAMP-project/dspot",
			"ftomassetti/turin-programming-language"
		);

		new AnalyzeRepositoryHistory(
			"javaparser", "javaparser", jpClients, jpProps, jpModule, Path.of("./data"))
			.analyzePRs(2000, 4);
	}

	public static void main(String[] args) throws Exception {
		//spoon();
		javaparser();
	}
}
