package com.github.maracas.rest.services;

import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.analysis.CommitAnalyzer;
import com.github.maracas.forges.analysis.PullRequestAnalysisResult;
import com.github.maracas.forges.analysis.PullRequestAnalyzer;
import com.github.maracas.forges.github.BreakbotConfig;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import japicmp.config.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Service
public class PullRequestService {
	private final BreakbotService breakbotService;
	private final Forge forge;
	private final PullRequestAnalyzer analyzer;

	private final Path reportPath;
	private final int cloneTimeout;
	private final int buildTimeout;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	public PullRequestService(Environment env, BreakbotService breakbotService, GitHub github) {
		this.breakbotService = breakbotService;
		this.forge = new GitHubForge(github);

		int analysisWorkers = env.getProperty("maracas.analysis-workers", Integer.class, -1);
		Path clonePath = Path.of(env.getProperty("maracas.clone-path", "./clones"));
		this.reportPath = Path.of(env.getProperty("maracas.report-path", "./reports"));
		this.buildTimeout = env.getProperty("maracas.build-timeout", Integer.class, 600);
		this.cloneTimeout = env.getProperty("maracas.clone-timeout", Integer.class, 600);

		clonePath.toFile().mkdirs();
		this.reportPath.toFile().mkdirs();

		CommitAnalyzer commitAnalyzer = analysisWorkers > 0
			? new CommitAnalyzer(Executors.newFixedThreadPool(analysisWorkers))
			: new CommitAnalyzer();
		this.analyzer = new PullRequestAnalyzer(clonePath, forge, commitAnalyzer);
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config = !StringUtils.isEmpty(breakbotYaml)
			? BreakbotConfig.fromYaml(breakbotYaml)
			: null;
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
		BreakbotConfig config = !StringUtils.isEmpty(breakbotYaml)
				? BreakbotConfig.fromYaml(breakbotYaml)
				: null;
		return buildMaracasReport(pr, config);
	}

	private MaracasReport buildMaracasReport(PullRequest pr, BreakbotConfig config) {
		logger.info("[{}] Starting the analysis", prUid(pr));

		PullRequestAnalysisResult result = analyzer.analyze(pr, makeMaracasOptions(config), config);

		/*
		FIXME:
		try {
			FileUtils.deleteDirectory(clonePathV1.toFile());
			FileUtils.deleteDirectory(clonePathV2.toFile());
		} catch (IOException e) {
			logger.error(e);
		}*/

		return MaracasReport.of(result);
	}

	private MaracasOptions makeMaracasOptions(BreakbotConfig config) {
		MaracasOptions options = MaracasOptions.newDefault();
		Options jApiOptions = options.getJApiOptions();
		config.excludes().forEach(excl -> jApiOptions.addExcludeFromArgument(japicmp.util.Optional.of(excl), false));
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
}
