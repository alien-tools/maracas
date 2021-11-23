package com.github.maracas.rest.services;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.delta.PullRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
	@Value("${maracas.breakbot-file:.breakbot.yml}")
	private String breakbotFile;

	@PostConstruct
	public void initialize() {
		Paths.get(clonePath).toFile().mkdirs();
		Paths.get(reportPath).toFile().mkdirs();
	}

	public String analyzePR(String owner, String repository, int prId, String callback, String installationId, String breakbotYaml) {
		GHRepository repo = githubService.getRepository(owner, repository);
		GHPullRequest pr = githubService.getPullRequest(owner, repository, prId);
		String prHead = pr.getHead().getSha();
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				readBreakbotConfig(repo) :
				BreakbotConfig.fromYaml(breakbotYaml);
		PullRequest prDiff = new PullRequest(pr, config, clonePath, maracasService);
		String uid = prUid(owner, repository, prId, prHead);
		File reportFile = reportFile(owner, repository, prId, prHead);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(owner, repository, prId);

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

	public MaracasReport analyzePRSync(String owner, String repository, int prId, String breakbotYaml) {
		GHRepository repo = githubService.getRepository(owner, repository);
		GHPullRequest pr = githubService.getPullRequest(repo, prId);
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				readBreakbotConfig(repo) :
				BreakbotConfig.fromYaml(breakbotYaml);
		PullRequest prDiff = new PullRequest(pr, config, clonePath, maracasService);

		return prDiff.diff();
	}

	public MaracasReport getReport(String owner, String repository, int id, String head) {
		try {
			File reportFile = reportFile(owner, repository, id, head);
			if (reportFile.exists() && reportFile.length() > 0) {
				return MaracasReport.fromJson(reportFile);
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return null;
	}

	public MaracasReport getReport(String owner, String repository, int id) {
		GHPullRequest pr = githubService.getPullRequest(owner, repository, id);
		String head = pr.getHead().getSha();

		return getReport(owner, repository, id, head);
	}

	public boolean isProcessing(String owner, String repository, int id, String head) {
		return jobs.containsKey(prUid(owner, repository, id, head));
	}

	public boolean isProcessing(String owner, String repository, int id) {
		GHPullRequest pr = githubService.getPullRequest(owner, repository, id);
		String head = pr.getHead().getSha();

		return isProcessing(owner, repository, id, head);
	}

	private BreakbotConfig readBreakbotConfig(GHRepository repo) {
		try (InputStream configIn = repo.getFileContent(breakbotFile).read()) {
			BreakbotConfig res = BreakbotConfig.fromYaml(configIn);
			if (res != null)
				return res;
		} catch (@SuppressWarnings("unused") IOException e) {
			// shh
		}

		return BreakbotConfig.defaultConfig();
	}

	private String prUid(String repository, String user, int id, String head) {
		return repository + "-" + user + "-" + id + "-" + head;
	}

	private File reportFile(String owner, String repository, int id, String head) {
		return Paths.get(reportPath).resolve(owner).resolve(repository)
			.resolve(id + "-" + head + ".json").toFile();
	}
}
