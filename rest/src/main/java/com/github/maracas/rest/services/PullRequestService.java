package com.github.maracas.rest.services;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequest;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class PullRequestService {
	@Autowired
	private GitHubService githubService;
	@Autowired
	private MaracasService maracasService;
	@Autowired
	private BreakbotService breakbotService;
	@Autowired
	private BuildService buildService;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.report-path:./reports}")
	private String reportPath;

	@PostConstruct
	public void initialize() {
		Paths.get(clonePath).toFile().mkdirs();
		Paths.get(reportPath).toFile().mkdirs();
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
					githubService.readBreakbotConfig(pr) :
				BreakbotConfig.fromYaml(breakbotYaml);
		String uid = prUid(pr);
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.owner(), pr.repository(), pr.id());

		// If we're already on it, no need to compute it twice
		if (jobs.containsKey(uid))
			logger.info("{} is already being analyzed", uid);
		else {
			logger.info("Starting the analysis of {}", uid);

			CompletableFuture<Void> future =
				CompletableFuture
					.supplyAsync(() -> diff(pr, config))
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
						githubService.readBreakbotConfig(pr) :
						BreakbotConfig.fromYaml(breakbotYaml);
		return diff(pr, config);
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

	public boolean isProcessing(PullRequest pr) {
		return jobs.containsKey(prUid(pr));
	}

	private MaracasReport diff(PullRequest pr, BreakbotConfig config) {
		GHPullRequest ghPr = githubService.getPullRequest(pr);
		try {
			GHCommitPointer base = ghPr.getBase();
			GHCommitPointer head = ghPr.getHead();
			Path basePath = Paths.get(clonePath)
					.resolve(String.valueOf(base.getRepository().getId()))
					.resolve(base.getSha());
			Path headPath = Paths.get(clonePath)
					.resolve(String.valueOf(base.getRepository().getId()))
					.resolve(head.getSha());

			// Clone and build both repos
			CompletableFuture<Path> baseFuture = CompletableFuture.supplyAsync(
				() -> cloneAndBuild(base.getRepository().getHttpTransportUrl(), base.getRef(), basePath, config));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
				() -> cloneAndBuild(head.getRepository().getHttpTransportUrl(), head.getRef(), headPath, config));

			CompletableFuture.allOf(baseFuture, headFuture).join();
			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();

			return maracasService.makeReport(pr, base.getRef(), basePath, j1, j2, config);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private Path cloneAndBuild(String url, String ref, Path dest, BreakbotConfig config) {
		githubService.cloneRemote(url, ref, null, dest);
		buildService.build(dest, config.build());
		return buildService.locateJar(dest, config.build());
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

	private String prUid(PullRequest pr) {
		String head = githubService.getHead(pr);
		return pr.owner() + "-" + pr.repository() + "-" + pr.id() + "-" + head;
	}

	private File reportFile(PullRequest pr) {
		String head = githubService.getHead(pr);
		return Paths.get(reportPath).resolve(pr.owner()).resolve(pr.repository())
			.resolve(pr.id() + "-" + head + ".json").toFile();
	}
}
