package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.Detection;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneAndBuild;
import org.swat.maracas.rest.tasks.CloneException;
import org.swat.maracas.spoon.VersionAnalyzer;

public class PullRequest implements Diffable {
	private final GitHub github;
	private final BreakbotConfig config;
	private final GHPullRequest pr;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(PullRequest.class);

	public PullRequest(GHPullRequest pr, BreakbotConfig config, GitHub github, String clonePath) {
		this.github = github;
		this.config = config;
		this.pr = pr;
		this.clonePath = clonePath;
	}

	@Override
	public MaracasReport diff() {
		try {
			GHCommitPointer base = pr.getBase();
			GHCommitPointer head = pr.getHead();
			Path basePath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getRepository().getId()))
				.resolve(base.getSha());
			Path headPath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getRepository().getId()))
				.resolve(head.getSha());

			// Clone and build both repos
			CompletableFuture<Path> baseFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(base.getRepository().getHttpTransportUrl(), base.getRef(),
						basePath, config));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(head.getRepository().getHttpTransportUrl(), head.getRef(),
						headPath, config));
			CompletableFuture.allOf(baseFuture, headFuture).join();
			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();

			// Compute delta model
			VersionAnalyzer analyzer = new VersionAnalyzer(j1, j2);
			logger.info("Computing delta {} -> {}", j1, j2);
			analyzer.computeDelta();

			config.getGithubClients().parallelStream().forEach(c -> {
				try {
					// Clone the client
					logger.info("Cloning client {}", c);
					GHRepository clientRepo = github.getRepository(c);
					String clientBranch = clientRepo.getDefaultBranch();
					Path clientPath = Paths.get(clonePath)
						.resolve(clientRepo.getOwnerName())
						.resolve(clientRepo.getBranch(clientBranch).getSHA1());

					if (!clientPath.toFile().exists()) {
						String fullRef = "refs/heads/" + clientBranch; // FIXME
						CloneCommand clone =
							Git.cloneRepository()
								.setURI(clientRepo.getHttpTransportUrl())
								.setBranchesToClone(Collections.singletonList(fullRef))
								.setBranch(fullRef)
								.setDirectory(clientPath.toFile());

						try (Git g = clone.call()) {
							// Let me please try-with-resource without a variable :(
						} catch (GitAPIException e) {
							// Rethrow unchecked to get past
							// the Supplier interface
							throw new CloneException(e);
						}
					} else logger.info("{} exists. Skipping.", clientPath);

					logger.info("Computing detections on client {}", c);
					// FIXME: let the user configure the sources to analyse in the
					//        config file
					analyzer.analyzeClient(clientPath.resolve("src/main/java"));
				} catch (Exception e) {
					logger.error("Error building {}", c, e);
				}
			});

			return new MaracasReport(
				Delta.fromMaracasDelta(analyzer.getDelta()),
				analyzer.getDetections()
					.stream()
					.map(d -> Detection.fromMaracasDetection(d))
					.collect(Collectors.toSet())
			);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return new MaracasReport(e);
		} catch (BuildException | CloneException e) {
			return new MaracasReport(e);
		}
	}
}
