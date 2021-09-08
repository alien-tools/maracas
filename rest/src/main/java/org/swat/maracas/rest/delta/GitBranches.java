package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHBranch;
import org.swat.maracas.rest.BuildException;
import org.swat.maracas.rest.CloneException;
import org.swat.maracas.rest.MaracasService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.rest.tasks.CloneAndBuild;

public class GitBranches implements Diffable {
	private final MaracasService maracasService;
	private final BreakbotConfig config;
	private final GHBranch base;
	private final GHBranch head;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(GitBranches.class);

	public GitBranches(GHBranch base, GHBranch head, BreakbotConfig config, String clonePath, MaracasService maracasService) {
		this.base = base;
		this.head = head;
		this.config = config;
		this.clonePath = clonePath;
		this.maracasService = maracasService;
	}

	@Override
	public MaracasReport diff() {
		try {
			Path basePath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getOwner().getId()))
				.resolve(base.getSHA1());
			Path headPath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getOwner().getId()))
				.resolve(head.getSHA1());

			// Clone and build both repos
			CompletableFuture<Path> baseFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(base.getOwner().getHttpTransportUrl(), base.getName(),
						basePath, config));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(head.getOwner().getHttpTransportUrl(), head.getName(),
						headPath, config));
			CompletableFuture.allOf(baseFuture, headFuture).join();

			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();
			String repository = base.getOwner().getFullName();

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
