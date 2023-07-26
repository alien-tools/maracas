package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommitAnalyzer {
	private final Maracas maracas;
	private final ExecutorService executorService;

	private static final Logger logger = LogManager.getLogger(CommitAnalyzer.class);

	public CommitAnalyzer(Maracas maracas, ExecutorService executorService) {
		this.maracas = Objects.requireNonNull(maracas);
		this.executorService = Objects.requireNonNull(executorService);
	}

	public CommitAnalyzer(Maracas maracas) {
		this(maracas, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	}

	public CommitAnalyzer() {
		this(new Maracas());
	}

	public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options)
		throws CloneException, BuildException {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		Objects.requireNonNull(clients);
		Objects.requireNonNull(options);

		Delta delta = computeDelta(v1, v2, options);
		return computeImpact(delta, clients, options);
	}

	public Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options) throws CloneException, BuildException {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		Objects.requireNonNull(options);

		CompletableFuture<Optional<Path>> futureV1 = cloneAndBuildLibrary(v1, options);
		CompletableFuture<Optional<Path>> futureV2 = cloneAndBuildLibrary(v2, options);

		try {
			Optional<Path> jarV1 = futureV1.join();
			Optional<Path> jarV2 = futureV2.join();

			if (jarV1.isEmpty())
				throw new BuildException("Couldn't find the JAR built from " + v1.getCommit());
			if (jarV2.isEmpty())
				throw new BuildException("Couldn't find the JAR built from " + v2.getCommit());

			LibraryJar libV1 = LibraryJar.withSources(jarV1.get(), SourcesDirectory.of(v1.getModulePath()));
			LibraryJar libV2 = LibraryJar.withoutSources(jarV2.get());
			return maracas.computeDelta(libV1, libV2, options);
		} catch (CompletionException e) {
			// Simply unwrap and rethrow
			logger.error(e);
			throw e.getCause() instanceof RuntimeException cause ? cause : e;
		}
	}

	public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options) {
		Objects.requireNonNull(delta);
		Objects.requireNonNull(clients);
		Objects.requireNonNull(options);

		// If there are no BCs, there's no impact
		if (delta.isEmpty()) {
			return AnalysisResult.noImpact(
				delta,
				clients.stream().map(CommitBuilder::getClonePath).map(SourcesDirectory::of).toList()
			);
		}

		List<DeltaImpact> clientsImpact =
			clients.stream()
				.map(client -> cloneAndAnalyzeClient(delta, client, options))
				.map(CompletableFuture::join)
				.toList();

		return AnalysisResult.success(delta, clientsImpact);
	}

	private CompletableFuture<Optional<Path>> cloneAndBuildLibrary(CommitBuilder builder, MaracasOptions options) {
		return CompletableFuture.supplyAsync(
			() -> {
				builder.cloneCommit(options.getCloneTimeout());
				return builder.buildCommit(options.getBuildTimeout());
			},
			executorService
		);
	}

	private CompletableFuture<DeltaImpact> cloneAndAnalyzeClient(Delta delta, CommitBuilder builder, MaracasOptions options) {
		return CompletableFuture.supplyAsync(
			() -> {
				builder.cloneCommit(options.getCloneTimeout());
				return maracas.computeDeltaImpact(SourcesDirectory.of(builder.getModulePath()), delta, options);
			},
			executorService
		).exceptionally(e -> {
			logger.error(e);
			return DeltaImpact.error(SourcesDirectory.of(builder.getModulePath()), delta, e.getCause() != null ? e.getCause() : e);
		});
	}
}
