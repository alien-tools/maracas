package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHBranch;
import org.swat.maracas.rest.MaracasHelper;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneAndBuild;
import org.swat.maracas.rest.tasks.CloneException;

import io.usethesource.vallang.IList;

public class GItBranchesDiff implements Diffable {
	private final GHBranch base;
	private final GHBranch head;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(GItBranchesDiff.class);

	public GItBranchesDiff(GHBranch base, GHBranch head, String clonePath) {
		this.base = base;
		this.head = head;
		this.clonePath = clonePath;
	}

	@Override
	public Delta diff() {
		try {
			Path basePath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getOwner().getId()))
				.resolve(base.getSHA1());
			Path headPath = Paths.get(clonePath)
				.resolve(String.valueOf(base.getOwner().getId()))
				.resolve(head.getSHA1());

			// Clone and build both repos
			CompletableFuture<Path> baseFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(base.getOwner().getHttpTransportUrl(), base.getName(), basePath));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(head.getOwner().getHttpTransportUrl(), head.getName(), headPath));
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
