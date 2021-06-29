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
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.ImpactModel;
import org.swat.maracas.rest.delta.PullRequestDiff;
import org.swat.maracas.rest.impact.GithubRepository;

@Service
public class GithubService {
	@Autowired
	GitHub github;
	@Autowired
	MaracasService maracas;

	private Map<String, CompletableFuture<Delta>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(GithubService.class);

	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.delta-path:./deltas}")
	private String deltaPath;
	@Value("${maracas.breakbot-file:.breakbot.yml}")
	private String breakbotFile;

	@PostConstruct
	public void initialize() {
		Paths.get(clonePath).toFile().mkdirs();
		Paths.get(deltaPath).toFile().mkdirs();
	}

	public String analyzePR(String owner, String repository, int prId, String callback, String installationId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		String prHead = pr.getHead().getSha();
		BreakbotConfig config = readBreakbotConfig(repo);
		PullRequestDiff prDiff = new PullRequestDiff(maracas, config, pr, clonePath);
		String uid = prUid(owner, repository, prId, prHead);
		File deltaFile = deltaFile(owner, repository, prId, prHead);
		String deltaLocation = String.format("/github/pr/%s/%s/%s", owner, repository, prId);

		logger.info("Starting the analysis of {}", uid);

		// If we're already on it, no need to compute it twice
		if (!jobs.containsKey(uid) && !deltaFile.exists()) {
			CompletableFuture<Delta> future =
				prDiff.diffAsync()
				.thenApply(delta -> {
					// Compute impact
					config.getGithubClients().parallelStream().forEach(c -> computeAndWeaveImpact(delta, c));
					return delta;
				}).handle((delta, exc) -> {
					jobs.remove(uid);
					// Write down results to disk and/or notify BreakBot
					if (delta != null) {
						try {
							logger.info("Serializing {}", deltaFile);
							deltaFile.getParentFile().mkdirs();
							delta.writeJson(deltaFile);

							if (callback != null) {
								Breakbot bb = new Breakbot(new URI(callback), installationId);
								bb.sendPullRequestResponse(delta);
							}
						} catch (Exception e) {
							logger.error(e);
						}

						return delta;
					}

					logger.error(exc);
					return null;
				});

			jobs.put(uid, future);
		}

		return deltaLocation;
	}

	public String analyzePR(String owner, String repository, int prId) throws IOException {
		return analyzePR(owner, repository, prId, null, null);
	}

	public Delta analyzePRSync(String owner, String repository, int prId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		BreakbotConfig config = readBreakbotConfig(repo);
		PullRequestDiff prDiff = new PullRequestDiff(maracas, config, pr, clonePath);

		Delta delta = prDiff.diff();
		config.getGithubClients().parallelStream().forEach(c -> computeAndWeaveImpact(delta, c));

		return delta;
	}

	public Delta getDelta(String owner, String repository, int id, String head) {
		try {
			File deltaFile = deltaFile(owner, repository, id, head);
			if (deltaFile.exists() && deltaFile.length() > 0) {
				return Delta.fromJson(deltaFile);
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return null;
	}

	public Delta getDelta(String owner, String repository, int id) throws IOException {
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(id);
		String head = pr.getHead().getSha();

		return getDelta(owner, repository, id, head);
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

	private void computeAndWeaveImpact(Delta delta, String c) {
		try {
			GHRepository clientRepo = github.getRepository(c);
			GithubRepository client = new GithubRepository(maracas, clientRepo, clonePath);
			ImpactModel impact = client.computeImpact(delta);

			delta.weaveImpact(impact);
		} catch (IOException e) {
			logger.error(e);
		}
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

	private File deltaFile(String owner, String repository, int id, String head) {
		return Paths.get(deltaPath).resolve(owner).resolve(repository)
			.resolve(id + "-" + head + ".json").toFile();
	}
}
