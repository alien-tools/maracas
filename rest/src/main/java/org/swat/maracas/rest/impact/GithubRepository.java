package org.swat.maracas.rest.impact;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHRepository;
import org.swat.maracas.rest.MaracasHelper;
import org.swat.maracas.rest.data.BreakingChangeInstance;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.Detection;
import org.swat.maracas.rest.tasks.CloneAndBuild;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

public class GithubRepository implements Impactable {
	private final GHRepository repository;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(GithubRepository.class);

	public GithubRepository(GHRepository repository, String clonePath) {
		this.repository = repository;
		this.clonePath = clonePath;
	}

	@Override
	public Delta computeImpact(Delta delta) {
		try {
			Path clientPath = Paths.get(clonePath)
				.resolve(repository.getOwnerName())
				.resolve(repository.getBranch(repository.getDefaultBranch()).getSHA1());

			// Clone and build the client
			CompletableFuture<Path> clientFuture = CompletableFuture.supplyAsync(
				new CloneAndBuild(repository.getHttpTransportUrl(), repository.getDefaultBranch(), clientPath));
			Path clientJar = clientFuture.get();

			// Build impact model
			// FIXME: we should reuse the possibly-existing delta model,
			// but easier for now to just pass all the JARs to Maracas
			MaracasHelper maracas = MaracasHelper.getInstance();
			IList detections = maracas.computeImpact(delta.getJarV1(), delta.getJarV2(), clientJar, clientPath);
			detections.forEach(rascalDetection -> {
				Detection d = Detection.fromRascal((IConstructor) rascalDetection);
				Optional<BreakingChangeInstance> bc =
					delta.getBreakingChanges().stream()
					.filter(c -> c.getDeclaration().equals(d.getSrc()))
					.findFirst();

				d.setClient(repository.getHtmlUrl().toString());
				d.setPath(d.getPath().replaceFirst(clientPath.toAbsolutePath().toString(), ""));
				d.setUrl(
					String.format("%s/blob/%s/%s#L%s-L%s",
						repository.getHtmlUrl(), repository.getDefaultBranch(), d.getPath(), d.getStartLine(), d.getEndLine())
				);

				if (bc.isPresent())
					bc.get().addDetection(d);
				else
					logger.warn("Couldn't find matching BC for " + d.getElem() + " => " + d.getUsed());
			});

			return delta;
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return delta;
		} catch (IOException e) {
			logger.error(e);
			return delta;
		}
	}
}
