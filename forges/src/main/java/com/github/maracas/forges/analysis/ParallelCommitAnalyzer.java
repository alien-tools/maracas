package com.github.maracas.forges.analysis;

import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.report.ClientImpact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParallelCommitAnalyzer implements CommitAnalyzer {
	private final ExecutorService executorService;
	private static final Logger logger = LogManager.getLogger(ParallelCommitAnalyzer.class);

	public ParallelCommitAnalyzer() {
		this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public ParallelCommitAnalyzer(ExecutorService executorService) {
		this.executorService = Objects.requireNonNull(executorService);
	}

	@Override
	public Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options)
		throws BuildException, CloneException {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		Objects.requireNonNull(options);

		CompletableFuture<Optional<Path>> futureV1 =
			CompletableFuture
				.supplyAsync(
					() -> {
						v1.cloneCommit(options.getCloneTimeoutSeconds());
						return v1.buildCommit(options.getBuildTimeoutSeconds());
					},
					executorService);
		CompletableFuture<Optional<Path>> futureV2 =
			CompletableFuture
				.supplyAsync(
					() -> {
						v2.cloneCommit(options.getCloneTimeoutSeconds());
						return v2.buildCommit(options.getBuildTimeoutSeconds());
					},
					executorService);

		try {
			CompletableFuture.allOf(futureV1, futureV2).join();
			Optional<Path> jarV1 = futureV1.get();
			Optional<Path> jarV2 = futureV2.get();

			if (jarV1.isEmpty())
				throw new BuildException("Couldn't find the JAR built from " + v1.getCommit());
			if (jarV2.isEmpty())
				throw new BuildException("Couldn't find the JAR built from " + v2.getCommit());

			LibraryJar libV1 = new LibraryJar(jarV1.get(), new SourcesDirectory(v1.getModulePath()));
			LibraryJar libV2 = new LibraryJar(jarV2.get());
			return Maracas.computeDelta(libV1, libV2, options);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException | CompletionException e) {
			// Simply unwrap
			if (e.getCause() instanceof BuildException be)
				throw be;
			if (e.getCause() instanceof CloneException ce)
				throw ce;
			logger.error(e);
		}

		return null;
	}

	@Override
	public List<ClientImpact> computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options) {
		Objects.requireNonNull(delta);
		Objects.requireNonNull(clients);
		Objects.requireNonNull(options);

		if (delta.getBreakingChanges().isEmpty()) {
			return Collections.emptyList();
		}

		Map<Commit, CompletableFuture<ClientImpact>> clientFutures =
			clients.stream()
				.collect(
					Collectors.toMap(
						CommitBuilder::getCommit,
						c -> CompletableFuture.supplyAsync(
							() -> computeImpact(delta, c, options),
							executorService
						))
				);

		CompletableFuture.allOf(clientFutures.values().toArray(CompletableFuture[]::new)).join();
		List<ClientImpact> results = new ArrayList<>();
		for (Map.Entry<Commit, CompletableFuture<ClientImpact>> future : clientFutures.entrySet()) {
			try {
				results.add(future.getValue().get());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				results.add(ClientImpact.error(future.getKey(), e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
			}
		}

		return results;
	}

	private ClientImpact computeImpact(Delta delta, CommitBuilder client, MaracasOptions options)
		throws CloneException {
		if (delta.getBreakingChanges().isEmpty())
			return ClientImpact.noImpact(client.getCommit());

		try {
			client.cloneCommit(options.getCloneTimeoutSeconds());
			SourcesDirectory clientSources = new SourcesDirectory(client.getModulePath());
			DeltaImpact impact = Maracas.computeDeltaImpact(clientSources, delta, options);
			return ClientImpact.success(client.getCommit(), impact, client.getClonePath());
		} catch (CloneException e) {
			return ClientImpact.error(client.getCommit(), e.getMessage());
		}
	}
}