package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swat.maracas.rest.breakbot.BreakBot;
import org.swat.maracas.rest.data.Config;
import org.swat.maracas.rest.data.Delta;
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

	public String analyzePR(String owner, String repository, int prId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		PullRequestDiff prDiff = new PullRequestDiff(maracas, pr, clonePath);
		Config config = readBreakbotConfig(repo);
		File deltaFile = Paths.get(deltaPath).resolve(owner).resolve(repository).resolve(prId + ".json").toFile();
		String uid = prUid(owner, repository, prId);
		String getLocation = String.format("/github/pr/%s/%s/%s", owner, repository, prId);

		if (!jobs.containsKey(uid) && !deltaFile.exists()) {
			CompletableFuture<Delta> future =
				prDiff.diffAsync()
				.thenApply(delta -> {
					for (String c : config.getGithubClients())
						try {
							GHRepository clientRepo = github.getRepository(c);
							GithubRepository client = new GithubRepository(maracas, clientRepo, clonePath);
							delta = client.computeImpact(delta);
						} catch (IOException e) {
							e.printStackTrace();
						}
					return delta;
				}).handle((delta, e) -> {
					jobs.remove(uid);
					if (delta != null) {
						logger.info("Serializing {}", deltaFile);
						deltaFile.getParentFile().mkdirs();
						delta.writeJson(deltaFile);

						return delta;
					} else if (e != null) {
						logger.error(e);
					}

					return null;
				});

			jobs.put(uid, future);
		}

		return getLocation;
	}

	public String analyzePR(String owner, String repository, int prId, String callback, int installationId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		PullRequestDiff prDiff = new PullRequestDiff(maracas, pr, clonePath);
		Config config = readBreakbotConfig(repo);
		String getLocation = String.format("/github/pr/%s/%s/%s", owner, repository, prId);

		CompletableFuture<Delta> future =
			prDiff.diffAsync()
			.thenApply(delta -> {
				for (String c : config.getGithubClients())
					try {
						GHRepository clientRepo = github.getRepository(c);
						GithubRepository client = new GithubRepository(maracas, clientRepo, clonePath);
						delta = client.computeImpact(delta);
					} catch (IOException e) {
						e.printStackTrace();
					}
				return delta;
			}).handle((delta, e) -> {
				if (delta != null) {
					try {
						BreakBot bb = new BreakBot(new URI(callback), installationId);
						bb.sendDelta(delta);
					} catch (URISyntaxException ee) {
						logger.error(ee);
					}

					return delta;
				} else if (e != null) {
					logger.error(e);
				}

				return null;
			});

		return getLocation;
	}

	public Delta analyzePRSync(String owner, String repository, int prId) throws IOException {
		// Read PR meta
		GHRepository repo = github.getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prId);
		PullRequestDiff prDiff = new PullRequestDiff(maracas, pr, clonePath);
		Config config = readBreakbotConfig(repo);
		Delta delta = prDiff.diff();

		for (String c : config.getGithubClients())
			try {
				GHRepository clientRepo = github.getRepository(c);
				GithubRepository client = new GithubRepository(maracas, clientRepo, clonePath);
				delta = client.computeImpact(delta);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return delta;
	}

	public Delta getPullRequest(String owner, String repository, int prId) {
		File deltaFile = Paths.get(deltaPath).resolve(owner).resolve(repository).resolve(prId + ".json").toFile();
		if (deltaFile.exists() && deltaFile.length() > 0) {
			return Delta.fromJson(deltaFile);
		}
		return null;
	}

	public boolean isProcessing(String owner, String repository, int prId) {
		return jobs.containsKey(prUid(owner, repository, prId));
	}

	public Config readBreakbotConfig(GHRepository repo) throws IOException {
		GHContent configFile = repo.getFileContent(breakbotFile);
		try (InputStream configIn = configFile.read()) {
			return Config.fromYaml(configIn);
		}
	}

	private String prUid(String repository, String user, int prId) {
		return repository + "-" + user + "-" + prId;
	}
}
