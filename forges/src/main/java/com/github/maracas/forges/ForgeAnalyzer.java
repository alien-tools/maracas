package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ForgeAnalyzer {
  private ExecutorService executorService = ForkJoinPool.commonPool();
  private int libraryBuildTimeout = Integer.MAX_VALUE;
  private int clientAnalysisTimeout = Integer.MAX_VALUE;

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    Delta delta = computeDelta(v1, v2, options);
    return computeImpact(delta, clients, options);
  }

  public Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    CompletableFuture<Optional<Path>> futureV1 =
      CompletableFuture
        .supplyAsync(v1::cloneAndBuildCommit, executorService)
        .orTimeout(libraryBuildTimeout, TimeUnit.SECONDS);
    CompletableFuture<Optional<Path>> futureV2 =
      CompletableFuture
        .supplyAsync(v2::cloneAndBuildCommit, executorService)
        .orTimeout(libraryBuildTimeout, TimeUnit.SECONDS);

    CompletableFuture.allOf(futureV1, futureV2).join();
    Optional<Path> jarV1 = futureV1.get();
    Optional<Path> jarV2 = futureV2.get();

    if (jarV1.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v1.getCommit());
    if (jarV2.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v2.getCommit());

    LibraryJar libV1 = new LibraryJar(jarV1.get(), new SourcesDirectory(v1.getClonePath()));
    LibraryJar libV2 = new LibraryJar(jarV2.get());
    return Maracas.computeDelta(libV1, libV2, options);
  }

  public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(delta);

    if (delta.getBreakingChanges().isEmpty()) {
      clients.forEach(c -> c.getClonePath().toFile().mkdirs());
      return AnalysisResult.noImpact(
        delta,
        clients.stream().map(c -> new SourcesDirectory(c.getClonePath())).toList()
      );
    }

    List<CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream().map(c ->
        CompletableFuture.supplyAsync(
          () -> {
            c.cloneCommit();
            return Maracas.computeDeltaImpact(new SourcesDirectory(c.getModulePath()), delta, options);
          },
          executorService
       ).orTimeout(clientAnalysisTimeout, TimeUnit.SECONDS)
        .exceptionally(t -> new DeltaImpact(new SourcesDirectory(c.getModulePath()), delta, t))
      ).toList();

    CompletableFuture.allOf(clientFutures.toArray(CompletableFuture[]::new)).join();

    Map<SourcesDirectory, DeltaImpact> impacts = new HashMap<>();
    for (CompletableFuture<DeltaImpact> future : clientFutures) {
      DeltaImpact impact = future.get();
      impacts.put(impact.getClient(), impact);
    }

    return new AnalysisResult(delta, impacts);
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void setLibraryBuildTimeout(int libraryBuildTimeout) {
    this.libraryBuildTimeout = libraryBuildTimeout;
  }

  public void setClientAnalysisTimeout(int clientAnalysisTimeout) {
    this.clientAnalysisTimeout = clientAnalysisTimeout;
  }
}
