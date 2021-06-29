package org.swat.maracas.rest.delta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.swat.maracas.rest.MaracasService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneAndBuild;
import org.swat.maracas.rest.tasks.CloneException;

import io.usethesource.vallang.IList;

public class PullRequestDiff implements Diffable {
	private final MaracasService maracas;
	private final BreakbotConfig config;
	private final GHPullRequest pr;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(PullRequestDiff.class);

	public PullRequestDiff(MaracasService maracas, BreakbotConfig config, GHPullRequest pr, String clonePath) {
		this.maracas = maracas;
		this.config = config;
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
			IList delta = maracas.computeDelta(j1, j2, basePath);

			Delta res = Delta.fromRascal(delta);
			res.setJarV1(j1);
			res.setJarV2(j2);
			res.setSources(basePath);
			// Set proper relative path and URLs
			res.getBreakingChanges().forEach(bc -> {
				bc.setPath(bc.getPath().replaceFirst(basePath.toAbsolutePath().toString(), ""));
				bc.setUrl(
					String.format("%s/blob/%s/%s#L%s-L%s",
						base.getRepository().getHtmlUrl(), base.getRef(), bc.getPath(), bc.getStartLine(), bc.getEndLine())
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
