package com.github.maracas.rest.services;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.BrokenUse;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import japicmp.config.Options;
import japicmp.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Service
public class PullRequestService {
	@Autowired
	private BreakbotService breakbotService;
	@Autowired
	private ClientsService clientsService;
	@Autowired
	private GitHub github;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.report-path:./reports}")
	private String reportPath;
	@Value("${maracas.analysis-workers:-1}")
	private int analysisWorkers;
	@Value("${maracas.build-timeout:600}")
	private int buildTimeout;
	@Value("${maracas.clone-timeout:600}")
	private int cloneTimeout;

	private Forge forge;
	private ForgeAnalyzer forgeAnalyzer;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	@PostConstruct
	public void initialize() {
		Path.of(clonePath).toFile().mkdirs();
		Path.of(reportPath).toFile().mkdirs();

		forge = new GitHubForge(github);
		forgeAnalyzer = new ForgeAnalyzer(forge, Path.of(clonePath));

		if (analysisWorkers > 0)
			forgeAnalyzer.setExecutorService(Executors.newFixedThreadPool(analysisWorkers));
		if (buildTimeout > 0)
			forgeAnalyzer.setBuildTimeoutSeconds(buildTimeout);
		if (cloneTimeout > 0)
			forgeAnalyzer.setCloneTimeoutSeconds(cloneTimeout);
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml)
				? breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name())
				: BreakbotConfig.fromYaml(breakbotYaml);
		String uid = prUid(pr);
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.repository().owner(), pr.repository().name(), pr.number());

		logger.info("Queuing analysis for {}", uid);
		CompletableFuture<Void> future =
			CompletableFuture
				.supplyAsync(() -> buildMaracasReport(pr, config))
				.handle((report, ex) -> {
					jobs.remove(uid);

					if (ex != null) {
						logger.error("Error analyzing {}", uid, ex);
						return PullRequestResponse.status(pr, ex.getCause().getMessage());
					}

					logger.info("Done analyzing {}", uid);
					serializeReport(report, reportFile);
					return PullRequestResponse.ok(pr, report);
				})
				.thenAccept(response -> {
					if (callback != null)
						breakbotService.sendPullRequestResponse(response, callback, installationId);
				});

		jobs.put(uid, future);
		return reportLocation;
	}

	public MaracasReport analyzePRSync(PullRequest pr, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml)
				? breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name())
				: BreakbotConfig.fromYaml(breakbotYaml);
		return buildMaracasReport(pr, config);
	}

	private MaracasReport buildMaracasReport(PullRequest pr, BreakbotConfig config) {
		logger.info("[{}] Starting the analysis", prUid(pr));

		MaracasOptions options = makeMaracasOptions(config);
		CommitBuilder baseBuilder = makeBuilderForCommit(pr, pr.mergeBase(), config.build());

		Map<String, Path> impactedPackages = forgeAnalyzer.inferImpactedPackages(pr, baseBuilder);
		logger.info("[{}] {} packages impacted: {}", prUid(pr), impactedPackages.size(), impactedPackages);

		List<PackageReport> packageReports = new ArrayList<>();
		impactedPackages.keySet().forEach(pkgName -> {
			try {
				Path modulePath = impactedPackages.get(pkgName);
				logger.info("[{}] Now analyzing package {}", prUid(pr), pkgName);

				// First, we compute the delta model to look for BCs
				CommitBuilder builderV1 = makeBuilderForCommit(pr, pr.mergeBase(), config.build(), modulePath);
				CommitBuilder builderV2 = makeBuilderForCommit(pr, pr.head(), config.build(), modulePath);
				Delta delta = forgeAnalyzer.computeDelta(builderV1, builderV2, options);

				// If we find some, we fetch the appropriate clients and analyze the impact
				if (!delta.getBreakingChanges().isEmpty()) {
					logger.info("[{}] Fetching clients for package {}", prUid(pr), pkgName);
					List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(pr.repository(), config.clients(), pkgName);
					logger.info("[{}] Found {} clients to analyze for package {}", prUid(pr), clients.size(), pkgName);

					Map<Path, CommitBuilder> clientBuilders = new HashMap<>();
					List<ClientReport> clientReports = new ArrayList<>();
					for (BreakbotConfig.GitHubRepository c : clients) {
						try {
							CommitBuilder clientBuilder = makeBuilderForClient(pr, c);
							clientBuilders.put(clientBuilder.getClonePath(), clientBuilder);
						} catch (IOException | ForgeException e) {
							logger.error("Couldn't create a builder for {}", c.repository(), e);
							clientReports.add(ClientReport.error(c.repository(), e.getMessage()));
						}
					}

					AnalysisResult result = forgeAnalyzer.computeImpact(delta, clientBuilders.values().stream().toList(), options);
					clientReports.addAll(
						result.deltaImpacts().keySet().stream()
							.map(client -> {
								CommitBuilder builder = clientBuilders.get(client);
								Repository clientRepo = builder.getCommit().repository();
								String clientName = clientRepo.owner() + "/" + clientRepo.name();
								DeltaImpact impact = result.deltaImpacts().get(client);
								Throwable t = impact.getThrowable();

								if (t != null)
									return ClientReport.error(clientName, t.getMessage());
								else
									return ClientReport.success(clientName,
										impact.getBrokenUses().stream()
											.map(bu -> BrokenUse.fromMaracasBrokenUse(bu, clientRepo, clientRepo.branch(), client))
											.toList());
							})
							.toList()
					);

					packageReports.add(PackageReport.success(
						pkgName,
						com.github.maracas.rest.data.Delta.fromMaracasDelta(delta, pr, builderV1.getClonePath()),
						clientReports
					));
				} else {
					packageReports.add(PackageReport.success(
						pkgName,
						com.github.maracas.rest.data.Delta.fromMaracasDelta(delta, pr, builderV1.getClonePath()),
						Collections.emptyList()
					));
				}
			} catch (Exception e) {
				logger.error(e);
				packageReports.add(PackageReport.error(pkgName, e.getMessage()));
			}
		});

		return new MaracasReport(packageReports);
	}

	private CommitBuilder makeBuilderForCommit(PullRequest pr, Commit c, BreakbotConfig.Build config) {
		Path commitClonePath = clonePath(pr, c);
		BuildConfig buildConfig = new BuildConfig(Path.of(config.module()));
		config.goals().forEach(buildConfig::addGoal);
		config.properties().keySet().forEach(k -> buildConfig.setProperty(k, config.properties().get(k)));

		return new CommitBuilder(c, commitClonePath, buildConfig);
	}

	private CommitBuilder makeBuilderForCommit(PullRequest pr, Commit c, BreakbotConfig.Build config, Path module) {
		Path commitClonePath = clonePath(pr, c);
		BuildConfig buildConfig = new BuildConfig(module);
		config.goals().forEach(buildConfig::addGoal);
		config.properties().keySet().forEach(k -> buildConfig.setProperty(k, config.properties().get(k)));

		return new CommitBuilder(c, commitClonePath, buildConfig);
	}

	private CommitBuilder makeBuilderForClient(PullRequest pr, BreakbotConfig.GitHubRepository c) throws IOException, ForgeException {
		String[] fields = c.repository().split("/");
		String clientOwner = fields[0];
		String clientName = fields[1];

		Repository clientRepo =
			StringUtils.isEmpty(c.branch())
				? forge.fetchRepository(clientOwner, clientName)
				: forge.fetchRepository(clientOwner, clientName, c.branch());

		String clientSha = github.getRepository(c.repository()).getBranch(clientRepo.branch()).getSHA1();
		Commit clientCommit =
			StringUtils.isEmpty(c.sha())
				? new Commit(clientRepo, clientSha)
				: new Commit(clientRepo, c.sha());
		Path clientClone = clonePath(pr, clientCommit);
		Path clientModule =
			c.module() != null
				? Path.of(c.module())
				: Path.of("");

		return new CommitBuilder(clientCommit, clientClone, new BuildConfig(clientModule));
	}

	private MaracasOptions makeMaracasOptions(BreakbotConfig config) {
		MaracasOptions options = MaracasOptions.newDefault();
		Options jApiOptions = options.getJApiOptions();
		config.excludes().forEach(excl -> jApiOptions.addExcludeFromArgument(Optional.of(excl), false));
		return options;
	}

	public boolean isProcessing(PullRequest pr) {
		return jobs.containsKey(prUid(pr));
	}

	private void serializeReport(MaracasReport report, File reportFile) {
		try {
			logger.info("Serializing {}", reportFile);
			reportFile.getParentFile().mkdirs();
			report.writeJson(reportFile);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public MaracasReport getReport(PullRequest pr) {
		try {
			File reportFile = reportFile(pr);
			if (reportFile.exists() && reportFile.length() > 0) {
				return MaracasReport.fromJson(reportFile);
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return null;
	}

	private String prUid(PullRequest pr) {
		return "%s-%s-%s-%s".formatted(
			pr.repository().owner(),
			pr.repository().name(),
			pr.number(),
			pr.head().sha()
		);
	}

	private File reportFile(PullRequest pr) {
		return Path.of(reportPath)
			.resolve(pr.repository().owner())
			.resolve(pr.repository().name())
			.resolve("%d-%s.json".formatted(pr.number(), pr.head().sha()))
			.toFile();
	}

	private Path clonePath(PullRequest pr, Commit c) {
		return Path.of(clonePath)
			.resolve(prUid(pr))
			.resolve(c.repository().owner())
			.resolve(c.repository().name())
			.resolve(c.sha())
			.resolve(RandomStringUtils.randomAlphanumeric(12))
			.toAbsolutePath();
	}
}
