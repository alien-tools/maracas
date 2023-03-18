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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CommitAnalyzer {
  private final ExecutorService executorService;

  private static final Logger logger = LogManager.getLogger(CommitAnalyzer.class);

  public CommitAnalyzer(ExecutorService executorService) {
    this.executorService = Objects.requireNonNull(executorService);
  }

  public CommitAnalyzer() {
    this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options)
    throws BuildException, CloneException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(clients);
    Objects.requireNonNull(options);

    Delta delta = computeDelta(v1, v2, options);
    return computeImpact(delta, clients, options);
  }

  public Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options)
    throws BuildException, CloneException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    CompletableFuture<Optional<Path>> futureV1 = cloneAndBuildLibrary(v1, options);
    CompletableFuture<Optional<Path>> futureV2 = cloneAndBuildLibrary(v2, options);

    try {
      CompletableFuture.allOf(futureV1, futureV2).join();
      Optional<Path> jarV1 = futureV1.get();
      Optional<Path> jarV2 = futureV2.get();

      if (jarV1.isEmpty())
        throw new BuildException("Couldn't find the JAR built from " + v1.getCommit());
      if (jarV2.isEmpty())
        throw new BuildException("Couldn't find the JAR built from " + v2.getCommit());

      LibraryJar libV1 = LibraryJar.withSources(jarV1.get(), SourcesDirectory.of(v1.getModulePath()));
      LibraryJar libV2 = LibraryJar.withoutSources(jarV2.get());
      return Maracas.computeDelta(libV1, libV2, options);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException | CompletionException e) {
      // Simply unwrap and rethrow
      if (e.getCause() instanceof BuildException be)
        throw be;
      if (e.getCause() instanceof CloneException ce)
        throw ce;
      logger.error(e);
    }

    return null;
  }

  public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options) {
    Objects.requireNonNull(delta);
    Objects.requireNonNull(clients);
    Objects.requireNonNull(options);

    // If there are no BCs, there's no impact
    if (delta.getBreakingChanges().isEmpty()) {
      return AnalysisResult.noImpact(
        delta,
        clients.stream().map(c -> SourcesDirectory.of(c.getClonePath())).toList()
      );
    }

    Map<Path, CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream()
        .collect(Collectors.toMap(
          CommitBuilder::getModulePath,
          c -> cloneAndAnalyzeClient(delta, c, options)
        ));

    CompletableFuture.allOf(clientFutures.values().toArray(CompletableFuture[]::new)).join();

    Map<Path, DeltaImpact> impacts = new HashMap<>();
    for (Map.Entry<Path, CompletableFuture<DeltaImpact>> entry : clientFutures.entrySet()) {
      try {
        impacts.put(entry.getKey(), entry.getValue().get());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        SourcesDirectory client = SourcesDirectory.of(entry.getKey());
        impacts.put(entry.getKey(),
          DeltaImpact.error(client, delta, e.getCause() != null ? e.getCause() : e));
      }
    }

    return AnalysisResult.success(delta, impacts);
  }

  private CompletableFuture<Optional<Path>> cloneAndBuildLibrary(CommitBuilder builder, MaracasOptions options) {
    return CompletableFuture.supplyAsync(
      () -> {
        builder.cloneCommit(options.getCloneTimeoutSeconds());
        return builder.buildCommit(options.getBuildTimeoutSeconds());
      },
      executorService);
  }

  private CompletableFuture<DeltaImpact> cloneAndAnalyzeClient(Delta delta, CommitBuilder builder, MaracasOptions options) {
    return CompletableFuture.supplyAsync(
      () -> {
        try {
          builder.cloneCommit(options.getCloneTimeoutSeconds());
        } catch (CloneException e) {
          return DeltaImpact.error(SourcesDirectory.of(builder.getModulePath()), delta, e);
        }

        return Maracas.computeDeltaImpact(SourcesDirectory.of(builder.getModulePath()), delta, options);
      },
      executorService
    );
  }
}
