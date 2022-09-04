package com.github.maracas.forges;

import com.github.maracas.*;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class ForgeAnalyzer {
  private ExecutorService executorService = ForkJoinPool.commonPool();

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    Delta delta = computeDelta(v1, v2, options);
    return computeImpact(delta, clients);
  }

  public Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    CompletableFuture<Optional<Path>> futureV1 = CompletableFuture.supplyAsync(v1::cloneAndBuildCommit, executorService);
    CompletableFuture<Optional<Path>> futureV2 = CompletableFuture.supplyAsync(v2::cloneAndBuildCommit, executorService);

    CompletableFuture.allOf(futureV1, futureV2).join();
    Optional<Path> jarV1 = futureV1.get();
    Optional<Path> jarV2 = futureV2.get();

    if (jarV1.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v1.getCommit());
    if (jarV2.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v2.getCommit());

    LibraryJar libV1 = new LibraryJar(jarV1.get(), new SourcesDirectory(v1.getClonePath()));
    LibraryJar libV2 = new LibraryJar(jarV2.get());
    Delta delta = Maracas.computeDelta(libV1, libV2, options);
    delta.populateLocations();
    return delta;
  }

  public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(delta);

    if (delta.getBreakingChanges().isEmpty())
      return AnalysisResult.noImpact(
        delta,
        clients.stream().map(c -> new SourcesDirectory(c.getClonePath())).toList()
      );

    List<CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream().map(c ->
        CompletableFuture.supplyAsync(
          () -> {
            c.cloneCommit();
            return Maracas.computeDeltaImpact(new SourcesDirectory(c.getModulePath()), delta);
          },
          executorService
       ).exceptionally(t -> new DeltaImpact(new SourcesDirectory(c.getModulePath()), delta, t))
      ).toList();

    CompletableFuture.allOf(clientFutures.toArray(CompletableFuture[]::new)).join();

    Map<SourcesDirectory, DeltaImpact> impacts = new HashMap<>();
    for (CompletableFuture<DeltaImpact> future : clientFutures) {
      DeltaImpact impact = future.get();
      impacts.put(impact.getClient(), impact);
    }

    return new AnalysisResult(delta, impacts);
  }
}
