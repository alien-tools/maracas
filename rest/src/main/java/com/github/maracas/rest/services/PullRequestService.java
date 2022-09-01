package com.github.maracas.rest.services;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.BrokenUse;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import japicmp.config.Options;
import japicmp.util.Optional;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PullRequestService {
	@Autowired
	private BreakbotService breakbotService;
	@Autowired
	private GitHub github;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.report-path:./reports}")
	private String reportPath;
	@Value("${maracas.threads:-1}")
	private int nThreads;

	private ExecutorService executorService;
	private Forge forge;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	@PostConstruct
	public void initialize() {
		Path.of(clonePath).toFile().mkdirs();
		Path.of(reportPath).toFile().mkdirs();

		executorService = nThreads == -1
			? Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
			: Executors.newFixedThreadPool(nThreads);

		forge = new GitHubForge(github);
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name()) :
				BreakbotConfig.fromYaml(breakbotYaml);
		String uid = prUid(pr);
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.repository().owner(), pr.repository().name(), pr.number());

		// If we're already on it, no need to compute it twice
		if (isProcessing(pr))
			logger.info("{} is already being analyzed", uid);
		else {
			logger.info("Queuing analysis for {}", uid);
			CompletableFuture<Void> future =
				CompletableFuture
					.supplyAsync(() -> buildMaracasReport(pr, config), executorService)
					.handle((report, ex) -> {
						jobs.remove(uid);

						if (ex != null) {
							logger.error("Error analyzing " + uid, ex);
							return new PullRequestResponse(ex.getCause().getMessage());
						}

						logger.info("Done analyzing {}", uid);
						serializeReport(report, reportFile);
						return new PullRequestResponse("ok", report);
					})
					.thenAccept(response -> {
						if (callback != null)
							breakbotService.sendPullRequestResponse(response, callback, installationId);
					});

			jobs.put(uid, future);
		}

		return reportLocation;
	}

	public MaracasReport analyzePRSync(PullRequest pr, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name()) :
				BreakbotConfig.fromYaml(breakbotYaml);
		return buildMaracasReport(pr, config);
	}

	private MaracasReport buildMaracasReport(PullRequest pr, BreakbotConfig config) {
		logger.info("Starting the analysis for {}", prUid(pr));

		try {
			CommitBuilder baseBuilder = builderFor(pr, pr.mergeBase(), config);
			CommitBuilder headBuilder = builderFor(pr, pr.head(), config);

			Map<Path, CommitBuilder> clientBuilders = new HashMap<>();
			List<ClientReport> clientReports = new ArrayList<>();
			for (BreakbotConfig.GitHubRepository c : config.clients()) {
				String[] fields = c.repository().split("/");
				String clientOwner = fields[0];
				String clientName = fields[1];

				try {
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

					CommitBuilder clientBuilder = new CommitBuilder(clientCommit, clientClone, clientModule);
					clientBuilders.put(clientClone, clientBuilder);
				} catch (Exception e) {
					clientReports.add(ClientReport.error(clientName, e));
				}
			}

			MaracasOptions options = MaracasOptions.newDefault();
			Options jApiOptions = options.getJApiOptions();
			config.excludes().forEach(excl -> jApiOptions.addExcludeFromArgument(Optional.of(excl), false));
			ForgeAnalyzer analyzer = new ForgeAnalyzer();
			AnalysisResult result = analyzer.analyzeCommits(baseBuilder, headBuilder, clientBuilders.values().stream().toList(), options);

			clientReports.addAll(
				result.deltaImpacts().keySet().stream()
					.map(client -> {
						CommitBuilder builder = clientBuilders.get(client.getLocation());
						Repository clientRepo = builder.getCommit().repository();
						String clientName = clientRepo.owner() + "/" + clientRepo.name();
						DeltaImpact impact = result.deltaImpacts().get(client);
						Throwable t = impact.getThrowable();

						if (t != null)
							return ClientReport.error(clientName, t);
						else
							return ClientReport.success(clientName,
								impact.getBrokenUses().stream()
									.map(bu -> BrokenUse.fromMaracasBrokenUse(bu, clientRepo, clientRepo.branch(), client.getLocation()))
									.toList());
					})
					.toList()
			);

			return new MaracasReport(
				com.github.maracas.rest.data.Delta.fromMaracasDelta(result.delta(), pr, clonePath(pr, pr.mergeBase())),
				clientReports
			);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private CommitBuilder builderFor(PullRequest pr, Commit c, BreakbotConfig config) {
		Path clonePath = clonePath(pr, c);
		BuildConfig buildConfig = new BuildConfig(clonePath, Path.of(config.build().module()));
		config.build().goals().forEach(g -> buildConfig.addGoal(g));
		config.build().properties().keySet().forEach(k -> buildConfig.setProperty(k, config.build().properties().get(k)));

		return new CommitBuilder(c, clonePath, buildConfig);
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
			.toAbsolutePath();
	}
}
