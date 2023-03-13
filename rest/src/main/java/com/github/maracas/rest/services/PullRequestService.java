package com.github.maracas.rest.services;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.BrokenUseDto;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.DeltaDto;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import japicmp.config.Options;
import japicmp.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Service
public class PullRequestService {
	private final BreakbotService breakbotService;
	private final ClientsService clientsService;
	private final GitHub github;
	private final Forge forge;
	private final ForgeAnalyzer forgeAnalyzer;


	private final Path clonePath;
	private final Path reportPath;
	private final int analysisWorkers;
	private final int cloneTimeout;
	private final int buildTimeout;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	public PullRequestService(Environment env, BreakbotService breakbotService, ClientsService clientsService, GitHub github) {
		this.breakbotService = breakbotService;
		this.clientsService = clientsService;
		this.github = github;
		this.forge = new GitHubForge(github);

		this.clonePath = Path.of(env.getProperty("maracas.clone-path", "./clones"));
		this.reportPath = Path.of(env.getProperty("maracas.report-path", "./reports"));
		this.analysisWorkers = env.getProperty("maracas.analysis-workers", Integer.class, -1);
		this.buildTimeout = env.getProperty("maracas.build-timeout", Integer.class, 600);
		this.cloneTimeout = env.getProperty("maracas.clone-timeout", Integer.class, 600);

		this.clonePath.toFile().mkdirs();
		this.reportPath.toFile().mkdirs();

		this.forgeAnalyzer = new ForgeAnalyzer(forge, clonePath);
		if (this.analysisWorkers > 0)
			forgeAnalyzer.setExecutorService(Executors.newFixedThreadPool(this.analysisWorkers));
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config = breakbotService.buildBreakbotConfig(pr.repository(), breakbotYaml);
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
					} else {
						logger.info("Done analyzing {}", uid);
						return PullRequestResponse.ok(pr, report);
					}
				})
				.thenAccept(response -> {
					serializeResponse(response, reportFile);
					if (callback != null)
						breakbotService.sendPullRequestResponse(response, callback, installationId);
				});

		jobs.put(uid, future);
		return reportLocation;
	}

	public MaracasReport analyzePRSync(PullRequest pr, String breakbotYaml) {
		BreakbotConfig config = breakbotService.buildBreakbotConfig(pr.repository(), breakbotYaml);
		return buildMaracasReport(pr, config);
	}

	private MaracasReport buildMaracasReport(PullRequest pr, BreakbotConfig config) {
		logger.info("[{}] Starting the analysis", prUid(pr));

		MaracasOptions options = makeMaracasOptions(config);
		Path clonePathV1 = newClonePath(pr, pr.mergeBase());
		Path clonePathV2 = newClonePath(pr, pr.head());
		CommitBuilder baseBuilder = makeBuilderForCommit(pr, pr.mergeBase(), config.build(), clonePathV1, Path.of(""));

		List<BuildModule> impactedPackages = forgeAnalyzer.inferImpactedPackages(pr, baseBuilder, options.getCloneTimeoutSeconds());
		logger.info("[{}] {} packages impacted: {}", prUid(pr), impactedPackages.size(), impactedPackages);

		List<PackageReport> packageReports = new ArrayList<>();
		impactedPackages.forEach(pkg -> {
			try {
				logger.info("[{}] Now analyzing package {}", prUid(pr), pkg.name());

				// First, we compute the delta model to look for BCs
				CommitBuilder builderV1 = makeBuilderForCommit(pr, pr.mergeBase(), config.build(), clonePathV1, pkg.path());
				CommitBuilder builderV2 = makeBuilderForCommit(pr, pr.head(), config.build(), clonePathV2, pkg.path());
				Delta delta = forgeAnalyzer.computeDelta(builderV1, builderV2, options);

				// If we find some, we fetch the appropriate clients and analyze the impact
				if (!delta.getBreakingChanges().isEmpty()) {
					logger.info("[{}] Fetching clients for package {}", prUid(pr), pkg.name());
					List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(pr.repository(), config.clients(), pkg.name());
					logger.info("[{}] Found {} clients to analyze for package {}", prUid(pr), clients.size(), pkg.name());

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
											.map(bu -> BrokenUseDto.of(bu, clientRepo, clientRepo.branch(), client))
											.toList());
							})
							.toList()
					);

					packageReports.add(PackageReport.success(
							pkg.name(),
						DeltaDto.of(delta, pr, builderV1.getClonePath()),
						clientReports
					));
				} else {
					packageReports.add(PackageReport.success(
							pkg.name(),
						DeltaDto.of(delta, pr, builderV1.getClonePath()),
						Collections.emptyList()
					));
				}
			} catch (Exception e) {
				logger.error(e);
				packageReports.add(PackageReport.error(pkg.name(), e.getMessage()));
			}
		});

		try {
			FileUtils.deleteDirectory(clonePathV1.toFile());
			FileUtils.deleteDirectory(clonePathV2.toFile());
		} catch (IOException e) {
			logger.error(e);
		}

		return new MaracasReport(packageReports);
	}

	private CommitBuilder makeBuilderForCommit(PullRequest pr, Commit c, BreakbotConfig.Build config, Path clonePath, Path module) {
		BuildConfig buildConfig = new BuildConfig(module);
		config.goals().forEach(buildConfig::addGoal);
		config.properties().keySet().forEach(k -> buildConfig.setProperty(k, config.properties().get(k)));

		return new CommitBuilder(c, clonePath, buildConfig);
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
		Path clientClone = newClonePath(pr, clientCommit);
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
		options.setCloneTimeoutSeconds(cloneTimeout);
		options.setBuildTimeoutSeconds(buildTimeout);

		return options;
	}

	public boolean isProcessing(PullRequest pr) {
		return jobs.containsKey(prUid(pr));
	}

	private void serializeResponse(PullRequestResponse report, File reportFile) {
		try {
			logger.info("Serializing {}", reportFile);
			reportFile.getParentFile().mkdirs();
			report.writeJson(reportFile);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public PullRequestResponse readResponse(PullRequest pr) {
		try {
			File responseFile = reportFile(pr);
			if (responseFile.exists() && responseFile.length() > 0) {
				return PullRequestResponse.fromJson(responseFile);
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
		return reportPath
			.resolve(pr.repository().owner())
			.resolve(pr.repository().name())
			.resolve("%d-%s.json".formatted(pr.number(), pr.head().sha()))
			.toFile();
	}

	private Path newClonePath(PullRequest pr, Commit c) {
		return clonePath
			.resolve(prUid(pr))
			.resolve(c.repository().owner())
			.resolve(c.repository().name())
			.resolve(c.sha())
			.resolve(RandomStringUtils.randomAlphanumeric(12))
			.toAbsolutePath();
	}
}
