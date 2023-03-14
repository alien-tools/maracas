package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
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
import org.apache.commons.lang3.RandomStringUtils;
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
  private final Path workingDirectory;
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  private static final Logger logger = LogManager.getLogger(CommitAnalyzer.class);

  public CommitAnalyzer(Path workingDirectory) {
    this.workingDirectory = Objects.requireNonNull(workingDirectory);
  }

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients,
                                       MaracasOptions options)
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

    CompletableFuture<Optional<Path>> futureV1 =
      CompletableFuture
        .supplyAsync(
          () -> v1.cloneAndBuildCommit(options.getCloneTimeoutSeconds(), options.getBuildTimeoutSeconds()),
          executorService);
    CompletableFuture<Optional<Path>> futureV2 =
      CompletableFuture
        .supplyAsync(
          () -> v2.cloneAndBuildCommit(options.getCloneTimeoutSeconds(), options.getBuildTimeoutSeconds()),
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

  public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options)
    throws CloneException {
    Objects.requireNonNull(delta);
    Objects.requireNonNull(clients);
    Objects.requireNonNull(options);

    if (delta.getBreakingChanges().isEmpty()) {
      clients.forEach(c -> c.getClonePath().toFile().mkdirs());
      return AnalysisResult.noImpact(
        delta,
        clients.stream().map(c -> new SourcesDirectory(c.getClonePath())).toList()
      );
    }

    Map<Path, CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream()
        .collect(Collectors.toMap(
          CommitBuilder::getModulePath,
          c -> CompletableFuture.supplyAsync(
            () -> {
              try {
                c.cloneCommit(options.getCloneTimeoutSeconds());
              } catch (CloneException e) {
                Path clientPath = c.getModulePath();
                clientPath.toFile().mkdirs();
                return new DeltaImpact(new SourcesDirectory(clientPath), delta, e);
              }

              return Maracas.computeDeltaImpact(new SourcesDirectory(c.getModulePath()), delta, options);
            },
            executorService
         )));

    CompletableFuture.allOf(clientFutures.values().toArray(CompletableFuture[]::new)).join();

    Map<Path, DeltaImpact> impacts = new HashMap<>();
    for (Map.Entry<Path, CompletableFuture<DeltaImpact>> entry : clientFutures.entrySet()) {
      try {
        impacts.put(entry.getKey(), entry.getValue().get());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        entry.getKey().toFile().mkdirs();
        SourcesDirectory client = new SourcesDirectory(entry.getKey());
        impacts.put(entry.getKey(),
          new DeltaImpact(client, delta, e.getCause() != null ? e.getCause() : e));
      }
    }

    return AnalysisResult.success(delta, impacts);
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = Objects.requireNonNull(executorService);
  }

  public Path newClonePath(Commit c) {
    return workingDirectory
      .resolve(c.repository().owner())
      .resolve(c.repository().name())
      .resolve(c.sha())
      .resolve(RandomStringUtils.randomAlphanumeric(12))
      .toAbsolutePath();
  }
}
