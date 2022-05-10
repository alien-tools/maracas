package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options)
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

    Delta delta = Maracas.computeDelta(jarV1.get(), jarV2.get(), options);
    if (delta.getBreakingChanges().isEmpty())
      return new AnalysisResult(
        delta,
        clients.stream()
          .collect(Collectors.toMap(
            builder -> builder.getClonePath(),
            builder -> new DeltaImpact(builder.getClonePath(), delta, Collections.emptySet())
          ))
      );

    delta.populateLocations(v1.getClonePath());

    Map<Path, CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream()
        .collect(Collectors.toMap(
          c -> c.getClonePath(),
          c -> CompletableFuture.supplyAsync(
            () -> {
              c.cloneCommit();
              return Maracas.computeDeltaImpact(c.getClonePath(), delta);
            },
            executorService
          ).exceptionally(t -> new DeltaImpact(c.getClonePath(), delta, t))
      ));

    CompletableFuture.allOf(clientFutures.values().toArray(CompletableFuture[]::new)).join();

    Map<Path, DeltaImpact> impacts = new HashMap<>();
    for (Map.Entry<Path, CompletableFuture<DeltaImpact>> entry : clientFutures.entrySet()) {
      impacts.put(entry.getKey(), entry.getValue().get());
    }

    return new AnalysisResult(delta, impacts);
  }
}
