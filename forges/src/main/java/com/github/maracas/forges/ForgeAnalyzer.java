package com.github.maracas.forges;

import com.github.maracas.AnalysisResult;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ForgeAnalyzer {
  private final Forge forge;
  private final Path workingDirectory;
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private int libraryBuildTimeoutSeconds = Integer.MAX_VALUE;
  private int clientAnalysisTimeoutSeconds = Integer.MAX_VALUE;

  private static final Logger logger = LogManager.getLogger(ForgeAnalyzer.class);

  public ForgeAnalyzer(Forge forge, Path workingDirectory) {
    Objects.requireNonNull(forge);
    Objects.requireNonNull(workingDirectory);
    this.forge = forge;
    this.workingDirectory = workingDirectory;
  }

  public ForgeAnalyzer(Forge forge, Path workingDirectory, ExecutorService executorService,
                       int libraryBuildTimeout, int clientAnalysisTimeout) {
    this(forge, workingDirectory);

    Objects.requireNonNull(executorService);
    if (libraryBuildTimeout < 0)
      throw new IllegalArgumentException("libraryBuildTimeout < 0");
    if (clientAnalysisTimeout < 0)
      throw new IllegalArgumentException("clientAnalysisTimeout < 0");

    this.executorService = executorService;
    this.libraryBuildTimeoutSeconds = libraryBuildTimeout;
    this.clientAnalysisTimeoutSeconds = clientAnalysisTimeout;
  }

  public List<AnalysisResult> analyzePullRequest(PullRequest pr, int clientsPerPackage, MaracasOptions options) {
    Objects.requireNonNull(pr);
    Objects.requireNonNull(options);
    if (clientsPerPackage < 0)
      throw new IllegalArgumentException("clientsPerPackage < 0");

    List<AnalysisResult> results = new ArrayList<>();
    Commit v1 = pr.mergeBase();
    Commit v2 = pr.head();
    Path v1Clone = clonePath(v1);
    Path v2Clone = clonePath(v2);
    Map<String, Path> impactedPackages = inferImpactedPackages(pr, makeBuilderForCommit(v1));
    logger.info("{} impacts {} packages: {}", pr, impactedPackages.size(), impactedPackages);

    // We need to run the whole analysis for each impacted package in the PR
    impactedPackages.keySet().forEach(pkgName -> {
      try {
        Path modulePath = impactedPackages.get(pkgName);
        logger.info("[{}] Now analyzing package {}", pr, pkgName);

        // First, we compute the delta model to look for BCs
        CommitBuilder builderV1 = new CommitBuilder(v1, v1Clone, new BuildConfig(modulePath));
        CommitBuilder builderV2 = new CommitBuilder(v2, v2Clone, new BuildConfig(modulePath));
        Delta delta = computeDelta(builderV1, builderV2, options);

        // If we find some, we fetch the appropriate clients and analyze the impact
        if (!delta.getBreakingChanges().isEmpty()) {
          logger.info("Fetching clients for package {}", pkgName);
          Collection<Commit> clients =
            forge.fetchTopClients(pr.repository(), pkgName, clientsPerPackage)
              .stream()
              .map(repository -> forge.fetchCommit(repository, "HEAD"))
              .toList();
          logger.info("Found {} clients to analyze for {}", clients.size(), pkgName);

          results.add(computeImpact(delta, clients.stream().map(this::makeBuilderForCommit).toList(), options));
        } else {
          results.add(AnalysisResult.noImpact(delta, Collections.emptyList()));
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Throwable t) {
        logger.error("Couldn't analyze package {}", pkgName, t);
      }
    });

    return results;
  }

  public AnalysisResult analyzeCommits(Commit v1, Commit v2, Collection<Commit> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(clients);
    Objects.requireNonNull(options);

    CommitBuilder builderV1 = makeBuilderForCommit(v1);
    CommitBuilder builderV2 = makeBuilderForCommit(v2);
    List<CommitBuilder> clientBuilders = clients.stream().map(this::makeBuilderForCommit).toList();

    return analyzeCommits(builderV1, builderV2, clientBuilders, options);
  }

  public AnalysisResult analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients,
                                       MaracasOptions options)
    throws InterruptedException, ExecutionException {
    Objects.requireNonNull(v1);
    Objects.requireNonNull(v2);
    Objects.requireNonNull(clients);
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
        .orTimeout(libraryBuildTimeoutSeconds, TimeUnit.SECONDS)
        // We catch to cleanup and rethrow as we can't get a JAR anyway
        .exceptionally(t -> {
          v1.cleanup();
          throw new CompletionException(t);
        });
    CompletableFuture<Optional<Path>> futureV2 =
      CompletableFuture
        .supplyAsync(v2::cloneAndBuildCommit, executorService)
        .orTimeout(libraryBuildTimeoutSeconds, TimeUnit.SECONDS)
        // We catch to cleanup and rethrow as we can't get a JAR anyway
        .exceptionally(t -> {
          v2.cleanup();
          throw new CompletionException(t);
        });

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
  }

  public AnalysisResult computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options)
    throws InterruptedException, ExecutionException {
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

    List<CompletableFuture<DeltaImpact>> clientFutures =
      clients.stream().map(c ->
        CompletableFuture.supplyAsync(
          () -> {
            c.cloneCommit();
            return Maracas.computeDeltaImpact(new SourcesDirectory(c.getModulePath()), delta, options);
          },
          executorService
       ).orTimeout(clientAnalysisTimeoutSeconds, TimeUnit.SECONDS)
        .exceptionally(t -> {
          logger.error("Error analyzing client {}", c, t);
          return new DeltaImpact(new SourcesDirectory(c.getModulePath()), delta, t);
        })
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
    Objects.requireNonNull(executorService);
    this.executorService = executorService;
  }

  public void setLibraryBuildTimeoutSeconds(int libraryBuildTimeoutSeconds) {
    if (libraryBuildTimeoutSeconds < 0)
      throw new IllegalArgumentException("libraryBuildTimeout < 0");

    this.libraryBuildTimeoutSeconds = libraryBuildTimeoutSeconds;
  }

  public void setClientAnalysisTimeoutSeconds(int clientAnalysisTimeoutSeconds) {
    if (clientAnalysisTimeoutSeconds < 0)
      throw new IllegalArgumentException("clientAnalysisTimeout < 0");

    this.clientAnalysisTimeoutSeconds = clientAnalysisTimeoutSeconds;
  }

  private CommitBuilder makeBuilderForCommit(Commit c) {
    return new CommitBuilder(c, clonePath(c), BuildConfig.newDefault());
  }

  public Map<String, Path> inferImpactedPackages(PullRequest pr, CommitBuilder builderV1) {
    builderV1.cloneCommit();
    Map<Path, String> modules = builderV1.getBuilder().locateModules();

    return pr.changedFiles()
      .stream()
      // We only want Java files that exist in 'v1', not the new files created by this PR
      .filter(f -> f.toString().endsWith(".java") && builderV1.getClonePath().resolve(f).toFile().exists())
      .map(f -> {
        Optional<Path> matchingPath =
          modules.keySet()
            .stream()
            .filter(p -> f.toString().startsWith(p.toString()))
            .max(Comparator.comparingInt((Path p) -> p.toString().length()));

        if (matchingPath.isPresent())
          return Map.entry(modules.get(matchingPath.get()), matchingPath.get());
        else {
          logger.warn("Couldn't infer the impacted package for {}", f);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
  }

  private Path clonePath(Commit c) {
    return workingDirectory
      .resolve(c.repository().owner())
      .resolve(c.repository().name())
      .resolve(c.sha())
      .resolve(RandomStringUtils.randomAlphanumeric(12))
      .toAbsolutePath();
  }

  private Path clonePath(PullRequest pr, Commit c) {
    return workingDirectory
      .resolve(prUid(pr))
      .resolve(c.repository().owner())
      .resolve(c.repository().name())
      .resolve(c.sha())
      .resolve(RandomStringUtils.randomAlphanumeric(12))
      .toAbsolutePath();
  }

  private String prUid(PullRequest pr) {
    return "%s-%s-%s-%s".formatted(
      pr.repository().owner(),
      pr.repository().name(),
      pr.number(),
      pr.head().sha()
    );
  }
}
