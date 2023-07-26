package com.github.maracas.rest.services;

import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.analysis.CommitAnalyzer;
import com.github.maracas.forges.analysis.PullRequestAnalyzer;
import com.github.maracas.forges.github.GitHubClientsScraper;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PullRequestService {
	private final BreakbotService breakbotService;
	private final Forge forge;
	private final PullRequestAnalyzer analyzer;

	private final Path reportPath;
	private final int cloneTimeout;
	private final int buildTimeout;
	private final int clientsPerModule;
	private final int maxClassLines;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	public PullRequestService(Environment env, BreakbotService breakbotService, GitHub github) {
		int analysisWorkers = env.getProperty("maracas.analysis-workers", Integer.class, -1);
		int clientsCacheExpiration = env.getProperty("maracas.clients-cache-expiration", Integer.class, 7);
		Path clonePath = Path.of(env.getProperty("maracas.clone-path", "./clones"));
		this.reportPath = Path.of(env.getProperty("maracas.report-path", "./reports"));
		this.buildTimeout = env.getProperty("maracas.build-timeout", Integer.class, 600);
		this.cloneTimeout = env.getProperty("maracas.clone-timeout", Integer.class, 600);
		this.clientsPerModule = env.getProperty("maracas.clients-per-module", Integer.class, 10);
		this.maxClassLines = env.getProperty("maracas.max-class-lines", Integer.class, 20_000);

		this.breakbotService = breakbotService;
		this.forge = new GitHubForge(github, new GitHubClientsScraper(Duration.ofDays(clientsCacheExpiration)));

		if ((!clonePath.toFile().exists() && !clonePath.toFile().mkdirs()) ||
			(!this.reportPath.toFile().exists() && !this.reportPath.toFile().mkdirs()))
			throw new IllegalStateException("Cannot create the necessary directories");

		ExecutorService executor = Executors.newFixedThreadPool(analysisWorkers > 0 ? analysisWorkers : Runtime.getRuntime().availableProcessors());
		CommitAnalyzer commitAnalyzer = new CommitAnalyzer(new Maracas(), executor);
		this.analyzer = new PullRequestAnalyzer(forge, commitAnalyzer, clonePath, executor);
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId) {
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.repository().owner(), pr.repository().name(), pr.number());

		logger.info("Queuing analysis for {}", pr.uid());
		CompletableFuture<Void> future =
			CompletableFuture
				.supplyAsync(() -> buildMaracasReport(pr))
				.handle((report, ex) -> {
					jobs.remove(pr.uid());

					if (ex != null) {
						logger.error("Error analyzing {}", pr.uid(), ex);
						return PullRequestResponse.status(pr, ex.getCause().getMessage());
					} else {
						logger.info("Done analyzing {}", pr.uid());
						return PullRequestResponse.ok(pr, report);
					}
				})
				.thenAccept(response -> {
					serializeResponse(response, reportFile);
					if (callback != null)
						breakbotService.sendPullRequestResponse(response, callback, installationId);
				});

		jobs.put(pr.uid(), future);
		return reportLocation;
	}

	public MaracasReport analyzePRSync(PullRequest pr) {
		return buildMaracasReport(pr);
	}

	private MaracasReport buildMaracasReport(PullRequest pr) {
		logger.info("[{}] Starting the analysis", pr.uid());
		return MaracasReport.of(analyzer.analyzePullRequest(pr, makeMaracasOptions()));
	}

	private MaracasOptions makeMaracasOptions() {
		MaracasOptions options = MaracasOptions.newDefault();
		options.setCloneTimeoutSeconds(cloneTimeout);
		options.setBuildTimeoutSeconds(buildTimeout);
		options.setClientsPerModule(clientsPerModule);
		options.setMaxClassLines(maxClassLines);

		return options;
	}

	public boolean isProcessing(PullRequest pr) {
		return jobs.containsKey(pr.uid());
	}

	private void serializeResponse(PullRequestResponse report, File reportFile) {
		try {
			logger.info("Serializing {}", reportFile);
			if (reportFile.getParentFile().exists() || reportFile.getParentFile().mkdirs())
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

	private File reportFile(PullRequest pr) {
		return reportPath.resolve(pr.uid() + ".json").toFile();
	}
}
