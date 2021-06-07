package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.swat.maracas.rest.MaracasHelper;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneAndBuild;
import org.swat.maracas.rest.tasks.CloneException;

import io.usethesource.vallang.IList;

public class PullRequestDiff implements Diffable {
	private final GHPullRequest pr;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(PullRequestDiff.class);

	public PullRequestDiff(GHPullRequest pr, String clonePath) {
		this.pr = pr;
		this.clonePath = clonePath;
	}

	@Override
	public Delta diff() {
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
					new CloneAndBuild(base.getRepository().getHttpTransportUrl(), base.getRef(), basePath));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(head.getRepository().getHttpTransportUrl(), head.getRef(), headPath));
			CompletableFuture.allOf(baseFuture, headFuture).join();
			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();

			// Build delta model
			MaracasHelper maracas = MaracasHelper.getInstance();
			IList delta = maracas.computeDelta(j1, j2, basePath);

			return Delta.fromRascal(delta);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return new Delta(null, e);
		} catch (BuildException | CloneException e) {
			return new Delta(null, e);
		}
	}
}
