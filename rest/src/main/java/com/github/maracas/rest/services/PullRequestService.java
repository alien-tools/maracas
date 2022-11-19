package com.github.maracas.rest.services;

import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.PullRequestAnalysisStrategy;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.Package;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.forges.report.PullRequestReport;
import com.github.maracas.rest.breakbot.BreakbotConfig;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
	@Value("${maracas.max-class-lines:20000}")
	private int maxClassLines;

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
				.supplyAsync(() -> buildPullRequestReport(pr, config))
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

	public PullRequestReport analyzePRSync(PullRequest pr, String breakbotYaml) {
		BreakbotConfig config = breakbotService.buildBreakbotConfig(pr.repository(), breakbotYaml);
		return buildPullRequestReport(pr, config);
	}

	private PullRequestReport buildPullRequestReport(PullRequest pr, BreakbotConfig config) {
		logger.info("[{}] Starting the analysis", prUid(pr));

		Map<Commit, BreakbotConfig.GitHubRepository> clientConfigs = new HashMap<>();
		return forgeAnalyzer.analyzePullRequest(
			pr,
			new PullRequestAnalysisStrategy() {
				@Override
				public BuildConfig makeLibraryConfig(Commit c, Path module) {
					BuildConfig buildConfig = new BuildConfig(module);
					config.build().goals().forEach(buildConfig::addGoal);
					config.build().properties().keySet().forEach(k -> buildConfig.setProperty(k, config.build().properties().get(k)));

					return buildConfig;
				}

				@Override
				public BuildConfig makeClientConfig(Commit c) {
					BreakbotConfig.GitHubRepository config = clientConfigs.get(c);

					if (config == null) {
						return BuildConfig.newDefault();
					} else {
						Path clientModule =
							config.module() != null
								? Path.of(config.module())
								: BuildConfig.DEFAULT_MODULE;

						return new BuildConfig(clientModule);
					}
				}

				@Override
				public List<Commit> fetchClientsFor(Package pkg) {
					List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(pr.repository(), config.clients(), pkg.id());
					logger.info("[{}] Found {} clients to analyze for package {}", prUid(pr), clients.size(), pkg.id());

					List<Commit> clientCommits = new ArrayList<>();
					for (BreakbotConfig.GitHubRepository c : clients) {
						try {
							Commit commit = getCommitForClient(c);
							clientCommits.add(commit);
							clientConfigs.putIfAbsent(commit, c);
						} catch (IOException | ForgeException e) {
							logger.error("Couldn't fetch client repository {}", c);
						}
					}

					return clientCommits;
				}
			},
			makeMaracasOptions(config)
		);
	}

	private Commit getCommitForClient(BreakbotConfig.GitHubRepository config) throws IOException, ForgeException {
		String[] fields = config.repository().split("/");
		String clientOwner = fields[0];
		String clientName = fields[1];

		Repository clientRepo =
			StringUtils.isEmpty(config.branch())
				? forge.fetchRepository(clientOwner, clientName)
				: forge.fetchRepository(clientOwner, clientName, config.branch());

		return forge.fetchCommit(clientRepo, StringUtils.isEmpty(config.sha()) ? "HEAD" : config.sha());
	}

	private MaracasOptions makeMaracasOptions(BreakbotConfig config) {
		MaracasOptions options = MaracasOptions.newDefault();
		Options jApiOptions = options.getJApiOptions();
		config.excludes().forEach(excl -> jApiOptions.addExcludeFromArgument(Optional.of(excl), false));
		options.setCloneTimeoutSeconds(cloneTimeout);
		options.setBuildTimeoutSeconds(buildTimeout);
		options.setMaxClassLines(maxClassLines);
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
		return Path.of(reportPath)
			.resolve(pr.repository().owner())
			.resolve(pr.repository().name())
			.resolve("%d-%s.json".formatted(pr.number(), pr.head().sha()))
			.toFile();
	}
}
