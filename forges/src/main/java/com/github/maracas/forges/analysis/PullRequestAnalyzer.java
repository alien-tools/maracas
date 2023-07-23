package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.BreakbotConfig;
import com.github.maracas.forges.github.GitHubModule;
import org.apache.commons.io.FileUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PullRequestAnalyzer {
	private final Forge forge;
	private final CommitAnalyzer commitAnalyzer;
  private final Path workingDirectory;
	private final ExecutorService executorService;

	private static final Logger logger = LogManager.getLogger(PullRequestAnalyzer.class);

	public PullRequestAnalyzer(Forge forge, CommitAnalyzer commitAnalyzer, Path workingDirectory, ExecutorService executorService) {
		this.forge = Objects.requireNonNull(forge);
		this.commitAnalyzer = Objects.requireNonNull(commitAnalyzer);
		this.workingDirectory = Objects.requireNonNull(workingDirectory);
		this.executorService = Objects.requireNonNull(executorService);
	}

	public PullRequestAnalyzer(Forge forge, CommitAnalyzer commitAnalyzer) {
		this(forge, commitAnalyzer, Path.of("pr-analysis"), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	}

	public PullRequestAnalyzer(Forge forge) {
		this(forge, new CommitAnalyzer());
	}

	public PullRequestAnalysisResult analyzePullRequest(PullRequest pr, MaracasOptions opts) {
		Objects.requireNonNull(pr);
		Objects.requireNonNull(opts);

		// Configuring the analysis according to BreakBot config file, if any
		MaracasOptions options = new MaracasOptions(opts);
		BreakbotConfig config = forge.fetchBreakbotConfig(pr.repository());
		config.excludes().forEach(excl -> options.getJApiOptions().addExcludeFromArgument(japicmp.util.Optional.of(excl), false));

		// First, we need to clone mergeBase
		CommitBuilder builderV1 = makeBuilderForLibrary(pr, new BuildModule("", Path.of("")), pr.mergeBase(), config.build());
		builderV1.cloneCommit(options.getCloneTimeoutSeconds());

		// Then, for every module in mergeBase that may be impacted by the PR
		List<BuildModule> impactedModules = inferImpactedModules(pr, builderV1);
		logger.info("{} impacts {} modules: {}", pr, impactedModules.size(), impactedModules);

		// We need to run the whole analysis for each impacted module in the PR
		List<ModuleAnalysisResult> results =
			impactedModules
				.stream()
				.map(module -> CompletableFuture.supplyAsync(() -> analyzeModule(pr, module, config.build(), options), executorService))
				.map(CompletableFuture::join)
				.toList();

		cleanUp(pr);
		return new PullRequestAnalysisResult(pr, results);
	}

	private ModuleAnalysisResult analyzeModule(PullRequest pr, BuildModule module, BreakbotConfig.Build buildConfig, MaracasOptions options) {
		List<GitHubModule> repositoryModules = forge.fetchModules(pr.repository());
		GitHubModule repositoryModule =
			repositoryModules
				.stream()
				.filter(m -> m.id().equals(module.name()))
				.findFirst()
				.orElse(new GitHubModule(pr.repository(), "unknown", "unknown"));

		try {
			logger.info("[{}] Now analyzing module {}", pr, module.name());

			// First, we compute the delta model to look for BCs
			Commit v1 = pr.mergeBase();
			Commit v2 = pr.head();
			CommitBuilder builderV1 = makeBuilderForLibrary(pr, module, v1, buildConfig);
			CommitBuilder builderV2 = makeBuilderForLibrary(pr, module, v2, buildConfig);
			Delta delta = commitAnalyzer.computeDelta(builderV1, builderV2, options);

			if (delta.isEmpty())
				return ModuleAnalysisResult.success(repositoryModule, delta, Collections.emptyMap(), builderV1.getClonePath());

			// If we find some, we fetch the appropriate clients and analyze the impact
			logger.info("Fetching clients for module {}", module.name());
			Collection<Commit> clients =
				forge.fetchAllClients(pr.repository(), module.name(), options.getClientsPerModule(), options.getMinStarsPerClient())
					.stream()
					.map(repository -> forge.fetchCommit(repository, "HEAD"))
					.toList();
			logger.info("Found {} clients to analyze for {}", clients.size(), module.name());

			Map<Commit, CommitBuilder> builders = new HashMap<>();
			clients.forEach(c -> builders.put(c, makeBuilderForClient(pr, module, c)));

			AnalysisResult result = commitAnalyzer.computeImpact(delta, builders.values(), options);
			return ModuleAnalysisResult.success(
				repositoryModule,
				delta,
				builders.keySet().stream().collect(Collectors.toMap(
					Commit::repository,
					c -> result.deltaImpacts().get(SourcesDirectory.of(builders.get(c).getClonePath()))
				)),
				builderV1.getClonePath()
			);
		} catch (Exception e) {
			return ModuleAnalysisResult.failure(repositoryModule, e.getMessage());
		}
	}

	public List<BuildModule> inferImpactedModules(PullRequest pr, CommitBuilder builder) {
		List<BuildModule> modules = builder.getBuilder().locateModules();

		return pr.changedFiles()
			.stream()
			// We only want Java files that exist in 'v1', not the new files created by this PR
			.filter(f -> f.toString().endsWith(".java") && builder.getClonePath().resolve(f).toFile().exists())
			.map(f -> {
				// Find the most nested module that matches the impacted file
				Optional<BuildModule> matchingModule =
					modules.stream()
						.filter(m -> f.toString().startsWith(m.path().toString()))
						.max(Comparator.comparingInt(m -> m.path().toString().length()));

				if (matchingModule.isEmpty())
					logger.warn("Couldn't infer the impacted module for {}", f);

				return matchingModule;
			})
			.flatMap(Optional::stream)
			.distinct()
			.toList();
	}

	private CommitBuilder makeBuilderForLibrary(PullRequest pr, BuildModule module, Commit c, BreakbotConfig.Build build) {
		BuildConfig buildConfig = new BuildConfig(module.path());
		build.goals().forEach(buildConfig::addGoal);
		build.properties().keySet().forEach(k -> buildConfig.setProperty(k, build.properties().get(k)));

		return new CommitBuilder(c, buildConfig, makeClonePath(pr, module, c));
	}

	private CommitBuilder makeBuilderForClient(PullRequest pr, BuildModule module, Commit c) {
		return new CommitBuilder(c, BuildConfig.newDefault(), makeClonePath(pr, module, c));
	}

  public Path makeClonePath(PullRequest pr, BuildModule module, Commit c) {
    return workingDirectory(pr)
	    .resolve(module.name().replace(":", "-"))
      .resolve(c.uid())
      .toAbsolutePath();
  }

	private Path workingDirectory(PullRequest pr) {
		return workingDirectory.resolve(pr.uid());
	}

	private void cleanUp(PullRequest pr) {
		FileUtils.deleteQuietly(workingDirectory(pr).toFile());
	}
}
