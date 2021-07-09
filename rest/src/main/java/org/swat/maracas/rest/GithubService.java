package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swat.maracas.rest.breakbot.Breakbot;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.rest.delta.PullRequest;

@Service
public class GithubService {
	@Autowired
	GitHub github;

	private Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(GithubService.class);

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

	public String analyzePR(String owner, String repository, int prId, String callback, String installationId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		String prHead = pr.getHead().getSha();
		BreakbotConfig config = readBreakbotConfig(repo);
		PullRequest prDiff = new PullRequest(pr, config, github, clonePath);
		String uid = prUid(owner, repository, prId, prHead);
		File reportFile = reportFile(owner, repository, prId, prHead);
		String reportLocation = String.format("/github/pr/%s/%s/%s", owner, repository, prId);

		logger.info("Starting the analysis of {}", uid);

		// If we're already on it, no need to compute it twice
		if (!jobs.containsKey(uid) && !reportFile.exists()) {
			CompletableFuture<Void> future =
				prDiff.diffAsync()
				.thenAccept(report -> {
					jobs.remove(uid);

					if (report.error() == null) {
						try {
							logger.info("Serializing {}", reportFile);
							reportFile.getParentFile().mkdirs();
							report.writeJson(reportFile);

							if (callback != null) {
								Breakbot bb = new Breakbot(new URI(callback), installationId);
								bb.sendPullRequestResponse(report);
							}
						} catch (Exception e) {
							logger.error(e);
						}
					} else
						logger.error(report.error());
				});

			jobs.put(uid, future);
		}

		return reportLocation;
	}

	public String analyzePR(String owner, String repository, int prId) throws IOException {
		return analyzePR(owner, repository, prId, null, null);
	}

	public MaracasReport analyzePRSync(String owner, String repository, int prId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		BreakbotConfig config = readBreakbotConfig(repo);
		PullRequest prDiff = new PullRequest(pr, config, github, clonePath);

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

	public MaracasReport getReport(String owner, String repository, int id) throws IOException {
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(id);
		String head = pr.getHead().getSha();

		return getReport(owner, repository, id, head);
	}

	public boolean isProcessing(String owner, String repository, int id, String head) {
		return jobs.containsKey(prUid(owner, repository, id, head));
	}

	public boolean isProcessing(String owner, String repository, int id) throws IOException {
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(id);
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
