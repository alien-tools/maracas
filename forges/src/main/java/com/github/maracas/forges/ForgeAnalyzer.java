package com.github.maracas.forges;

import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.CommitsReport;
import com.github.maracas.forges.report.PackageReport;
import com.github.maracas.forges.report.PullRequestReport;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

	public PullRequestReport analyzePullRequest(PullRequest pr, PullRequestAnalysisStrategy builderFactory, MaracasOptions options) {
		Objects.requireNonNull(pr);
		Objects.requireNonNull(builderFactory);
		Objects.requireNonNull(options);

		logger.info("Now analyzing {}", pr);

		// If there's nothing interesting, skip
		if (pr.changedJavaFiles().isEmpty())
			return PullRequestReport.success(pr, Collections.emptyList());

		// Pick the commits we're comparing: mergeBase vs head
		Commit v1 = pr.mergeBase();
		Commit v2 = pr.head();
		CommitBuilder builderV1 = new CommitBuilder(v1, newClonePath(v1), builderFactory.makeLibraryConfig(v1, BuildConfig.DEFAULT_MODULE));
		CommitBuilder builderV2 = new CommitBuilder(v2, newClonePath(v2), builderFactory.makeLibraryConfig(v2, BuildConfig.DEFAULT_MODULE));

		try {
			// List the updated files in v1
			builderV1.cloneCommit(options.getCloneTimeoutSeconds());
			List<Path> updatedFiles = pr.changedJavaFiles()
				.stream()
				.filter(f -> builderV1.getClonePath().resolve(f).toFile().exists())
				.toList();

			// For every package in v1 that's updated by the PR's changes
			List<Package> packages = builderV1.getBuilder().locatePackages();
			List<Package> updatedPackages = inferUpdatedPackages(packages, updatedFiles);
			logger.info("{} impacts {}/{} packages: {}", pr,
				updatedPackages.size(), packages.size(), updatedPackages);

			// We need to run the whole analysis for each updated package
			return PullRequestReport.success(
				pr,
				updatedPackages.stream()
					.map(pkg -> analyzePackage(pr, pkg, builderV1, builderV2, builderFactory, options))
					.toList()
			);
		} catch (Exception e) {
			return PullRequestReport.error(pr, e.getMessage());
		}
	}

	public PullRequestReport analyzePullRequest(PullRequest pr, MaracasOptions options) {
		return analyzePullRequest(
			pr,
			new PullRequestAnalysisStrategy() {
				@Override
				public BuildConfig makeLibraryConfig(Commit c, Path module) {
					return new BuildConfig(module);
				}

				@Override
				public BuildConfig makeClientConfig(Commit c) {
					return BuildConfig.newDefault();
				}

				@Override
				public List<Commit> fetchClientsFor(Package pkg) {
					List<Commit> clients =
						forge.fetchTopStarredClients(pr.repository(), pkg.id(), options.getClientsPerPackage(), options.getMinStarsPerClient())
							.stream()
							.map(repository -> forge.fetchCommit(repository, "HEAD"))
							.toList();
					logger.info("Found {} clients to analyze for {}", clients.size(), pkg.id());
					return clients;
				}
			},
			options
		);
	}

	public CommitsReport analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients,
	                                    MaracasOptions options)
		throws BuildException, CloneException {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		Objects.requireNonNull(clients);
		Objects.requireNonNull(options);

		try {
			Delta delta = computeDelta(v1, v2, options);
			List<ClientImpact> clientsImpact = computeImpact(delta, clients, options);
			return CommitsReport.success(v1.getCommit(), v2.getCommit(), delta, clientsImpact, v1.getClonePath());
		} catch (Exception e) {
			return CommitsReport.error(v1.getCommit(), v2.getCommit(), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}
	}

	private PackageReport analyzePackage(PullRequest pr, Package pkg, CommitBuilder builderV1, CommitBuilder builderV2, PullRequestAnalysisStrategy builderFactory, MaracasOptions options) {
		try {
			logger.info("Now analyzing {}", pkg);

			// First, we compute the delta model to look for BCs
			Commit v1 = builderV1.getCommit();
			Commit v2 = builderV2.getCommit();
			CommitBuilder pkgBuilderV1 = new CommitBuilder(v1, builderV1.getClonePath(), builderFactory.makeLibraryConfig(v1, pkg.modulePath()));
			CommitBuilder pkgBuilderV2 = new CommitBuilder(v2, builderV2.getClonePath(), builderFactory.makeLibraryConfig(v2, pkg.modulePath()));
			Delta delta = computeDelta(pkgBuilderV1, pkgBuilderV2, options);

			// If no BC, we stop here
			if (delta.getBreakingChanges().isEmpty())
				return PackageReport.success(pkg, delta, Collections.emptyList(), pr, pkgBuilderV1.getClonePath());

			// Otherwise we look at clients
			List<Commit> clients = builderFactory.fetchClientsFor(pkg);
			List<CommitBuilder> clientBuilders =
				clients.stream()
					.map(c -> new CommitBuilder(c, newClonePath(c), builderFactory.makeClientConfig(c)))
					.toList();
			List<ClientImpact> clientsImpact = computeImpact(delta, clientBuilders, options);
			return PackageReport.success(pkg, delta, clientsImpact, pr, pkgBuilderV1.getClonePath());
		} catch (Exception e) {
			return PackageReport.error(pkg, e.getMessage());
		}
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

	public void setExecutorService(ExecutorService executorService) {
		Objects.requireNonNull(executorService);
		this.executorService = executorService;
	}

	public List<Package> inferUpdatedPackages(List<Package> packages, List<Path> files) {
		Objects.requireNonNull(packages);
		Objects.requireNonNull(files);

		List<Package> impacted = new ArrayList<>();
		for (Path f : files) {
			Optional<Package> matchingPath =
				packages.stream()
					.filter(pkg -> f.toString().startsWith(pkg.modulePath().toString()))
					.max(Comparator.comparingInt(pkg -> pkg.modulePath().toString().length()));

			if (matchingPath.isPresent()) {
				Package pkg = matchingPath.get();
				if (!impacted.contains(pkg))
					impacted.add(pkg);
			} else {
				logger.warn("Couldn't infer the impacted package for {}", f);
			}
		}
		return impacted;
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