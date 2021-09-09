package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.swat.maracas.rest.BuildException;
import org.swat.maracas.rest.CloneException;
import org.swat.maracas.rest.MaracasService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.rest.tasks.CloneAndBuild;

public class PullRequest implements Diffable {
	private final MaracasService maracasService;
	private final BreakbotConfig config;
	private final GHPullRequest pr;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(PullRequest.class);

	public PullRequest(GHPullRequest pr, BreakbotConfig config, String clonePath, MaracasService maracasService) {
		this.config = config;
		this.pr = pr;
		this.clonePath = clonePath;
		this.maracasService = maracasService;
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
			String repository = base.getRepository().getFullName();

			return maracasService.makeReport(repository, basePath, j1, j2, config);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return new MaracasReport(e);
		} catch (BuildException | CloneException e) {
			return new MaracasReport(e);
		}
	}
}
