package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHBranch;
import org.swat.maracas.rest.MaracasService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneAndBuild;
import org.swat.maracas.rest.tasks.CloneException;

import io.usethesource.vallang.IList;

public class GitBranchesDiff implements Diffable {
	private final MaracasService maracas;
	private final BreakbotConfig config;
	private final GHBranch base;
	private final GHBranch head;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(GitBranchesDiff.class);

	public GitBranchesDiff(MaracasService maracas, BreakbotConfig config, GHBranch base, GHBranch head, String clonePath) {
		this.maracas = maracas;
		this.config = config;
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
					new CloneAndBuild(base.getOwner().getHttpTransportUrl(), base.getName(),
						basePath, config));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(
					new CloneAndBuild(head.getOwner().getHttpTransportUrl(), head.getName(),
						headPath, config));
			CompletableFuture.allOf(baseFuture, headFuture).join();
			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();

			// Build delta model
			IList delta = maracas.computeDelta(j1, j2, basePath);

			Delta res = Delta.fromRascal(delta);
			// Set proper relative path and URLs
			res.getBreakingChanges().forEach(bc -> {
				bc.setPath(bc.getPath().replaceFirst(basePath.toAbsolutePath().toString(), ""));
				bc.setUrl(
					String.format("%s/blob/%s/%s#L%s-L%s",
						base.getOwner().getHtmlUrl(), base.getName(), bc.getPath(), bc.getStartLine(), bc.getEndLine())
				);
			});
			return res;
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return new Delta(e);
		} catch (BuildException | CloneException e) {
			return new Delta(e);
		}
	}
}
