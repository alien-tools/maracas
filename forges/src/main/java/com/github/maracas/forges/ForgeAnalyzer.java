package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenUse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class ForgeAnalyzer {
  private ExecutorService executorService = ForkJoinPool.commonPool();

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, List<CommitBuilder> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(options);

    CompletableFuture<Optional<Path>> futureV1 = CompletableFuture.supplyAsync(
      () -> v1.cloneAndBuild(), executorService);
    CompletableFuture<Optional<Path>> futureV2 = CompletableFuture.supplyAsync(
      () -> v2.cloneAndBuild(), executorService);

    CompletableFuture.allOf(futureV1, futureV2).join();
    Optional<Path> jarV1 = futureV1.get();
    Optional<Path> jarV2 = futureV2.get();

    if (jarV1.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v1);
    if (jarV2.isEmpty())
      throw new BuildException("Couldn't build a JAR from " + v2);

    Delta delta = Maracas.computeDelta(jarV1.get(), jarV2.get(), options);
    if (delta.getBreakingChanges().isEmpty())
      return new AnalysisResult(
        delta,
        clients.stream()
          .collect(Collectors.toMap(
            c -> c.getSources(),
            c -> new DeltaImpact(c.getSources(), delta, Collections.emptySet())
          ))
      );

    delta.populateLocations(v1.getSources());

    List<CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream().map(c ->
        CompletableFuture.supplyAsync(
            () -> {
              c.clone();
              return Maracas.computeDeltaImpact(c.getSources(), delta);
            },
            executorService
        ).exceptionally(t -> new DeltaImpact(c.getSources(), delta, t))
      ).toList();

    CompletableFuture.allOf(clientFutures.toArray(CompletableFuture[]::new)).join();

    Map<Path, DeltaImpact> impacts = new HashMap<>();
    for (CompletableFuture<DeltaImpact> future : clientFutures) {
      DeltaImpact impact = future.get();
      impacts.put(impact.getClient(), impact);
    }

    return new AnalysisResult(delta, impacts);
  }
}
