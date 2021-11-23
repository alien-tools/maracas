package com.github.maracas.rest.services;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequest;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.delta.PullRequestDiff;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHPullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PullRequestService {
	@Autowired
	private GitHubService githubService;
	@Autowired
	private MaracasService maracasService;
	@Autowired
	private BreakbotService breakbotService;

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
		GHPullRequest ghPr = githubService.getPullRequest(pr);
		PullRequestDiff prDiff = new PullRequestDiff(pr, ghPr, config, clonePath, maracasService);
		String uid = prUid(pr);
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.owner(), pr.repository(), pr.id());

		// If we're already on it, no need to compute it twice
		if (jobs.containsKey(uid))
			logger.info("{} is already being analyzed", uid);
		else {
			logger.info("Starting the analysis of {}", uid);

			CompletableFuture<Void> future =
					prDiff.diffAsync()
						.exceptionally(ex -> {
							logger.error("Error analyzing " + uid, ex);
							if (callback != null)
								breakbotService.sendPullRequestResponse(new PullRequestResponse(ex.getCause().getMessage()), callback, installationId);
							return null;
						})
						.thenAccept(report -> {
							jobs.remove(uid);

							if (report != null) {
								logger.info("Done analyzing {}", uid);
								try {
									logger.info("Serializing {}", reportFile);
									reportFile.getParentFile().mkdirs();
									report.writeJson(reportFile);

									if (callback != null)
										breakbotService.sendPullRequestResponse(new PullRequestResponse("ok", report), callback, installationId);
								} catch (IOException e) {
									logger.error(e);
								}
							}
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
		GHPullRequest ghPr = githubService.getPullRequest(pr);
		PullRequestDiff prDiff = new PullRequestDiff(pr, ghPr, config, clonePath, maracasService);

		return prDiff.diff();
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
