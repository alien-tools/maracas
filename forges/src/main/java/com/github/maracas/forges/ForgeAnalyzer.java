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
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
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
import java.util.stream.Collectors;

public class ForgeAnalyzer {
  private final Forge forge;
  private final Path workingDirectory;
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  private static final Logger logger = LogManager.getLogger(ForgeAnalyzer.class);

  public ForgeAnalyzer(Forge forge, Path workingDirectory) {
    this.forge = Objects.requireNonNull(forge);
    this.workingDirectory = Objects.requireNonNull(workingDirectory);
  }

  public List<AnalysisResult> analyzePullRequest(PullRequest pr, MaracasOptions options) {
    Objects.requireNonNull(pr);
    Objects.requireNonNull(options);

    // For every package in mergeBase that may be impacted by the PR's changes
    Commit v1 = pr.mergeBase();
    Commit v2 = pr.head();
    Path v1Clone = newClonePath(v1);
    Path v2Clone = newClonePath(v2);
    CommitBuilder builderV1 = new CommitBuilder(v1, v1Clone, BuildConfig.newDefault());
    List<BuildModule> impactedPackages = inferImpactedPackages(pr, builderV1, options.getCloneTimeoutSeconds());
    logger.info("{} impacts {} packages: {}", pr, impactedPackages.size(), impactedPackages);

    // We need to run the whole analysis for each impacted package in the PR
    return impactedPackages.stream()
      .map(e -> analyzePullRequestPackage(pr, v1Clone, v2Clone, e, options))
      .toList();
  }

  private AnalysisResult analyzePullRequestPackage(PullRequest pr, Path v1Clone, Path v2Clone,
                                                   BuildModule pkg, MaracasOptions options) {
    try {
      logger.info("[{}] Now analyzing package {}", pr, pkg.name());

      Commit v1 = pr.mergeBase();
      Commit v2 = pr.head();

      // First, we compute the delta model to look for BCs
      CommitBuilder builderV1 = new CommitBuilder(v1, v1Clone, new BuildConfig(pkg.path()));
      CommitBuilder builderV2 = new CommitBuilder(v2, v2Clone, new BuildConfig(pkg.path()));
      Delta delta = computeDelta(builderV1, builderV2, options);

      if (delta.getBreakingChanges().isEmpty())
        return AnalysisResult.noImpact(delta, Collections.emptyList());

      // If we find some, we fetch the appropriate clients and analyze the impact
      logger.info("Fetching clients for package {}", pkg.name());
      Collection<Commit> clients =
        forge.fetchTopStarredClients(pr.repository(), pkg.name(), options.getClientsPerPackage(), options.getMinStarsPerClient())
          .stream()
          .map(repository -> forge.fetchCommit(repository, "HEAD"))
          .toList();
      logger.info("Found {} clients to analyze for {}", clients.size(), pkg.name());

      return computeImpact(delta, clients.stream().map(this::makeBuilderForCommit).toList(), options);
    } catch (Exception e) {
      return AnalysisResult.failure(e.getMessage());
    }
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

  private CommitBuilder makeBuilderForCommit(Commit c) {
    return new CommitBuilder(c, newClonePath(c), BuildConfig.newDefault());
  }

  public List<BuildModule> inferImpactedPackages(PullRequest pr, CommitBuilder builderV1, int cloneTimeoutSeconds) {
    builderV1.cloneCommit(cloneTimeoutSeconds);
    List<BuildModule> modules = builderV1.getBuilder().locateModules();

    return pr.changedFiles()
      .stream()
      // We only want Java files that exist in 'v1', not the new files created by this PR
      .filter(f -> f.toString().endsWith(".java") && builderV1.getClonePath().resolve(f).toFile().exists())
      .map(f -> {
        // Find the most nested module that matches the impacted file
        Optional<BuildModule> matchingModule =
          modules.stream()
            .filter(m -> f.toString().startsWith(m.path().toString()))
            .max(Comparator.comparingInt(m -> m.path().toString().length()));

        if (matchingModule.isEmpty())
          logger.warn("Couldn't infer the impacted package for {}", f);

        return matchingModule;
      })
      .flatMap(Optional::stream)
      .distinct()
      .toList();
  }

  private Path newClonePath(Commit c) {
    return workingDirectory
      .resolve(c.repository().owner())
      .resolve(c.repository().name())
      .resolve(c.sha())
      .resolve(RandomStringUtils.randomAlphanumeric(12))
      .toAbsolutePath();
  }
}