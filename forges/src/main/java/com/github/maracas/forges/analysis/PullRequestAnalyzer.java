package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildModule;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.github.BreakbotConfig;
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
import java.util.stream.Collectors;

public class PullRequestAnalyzer {
	private final Forge forge;
	private final CommitAnalyzer commitAnalyzer;
  private final Path workingDirectory;

	private static final Logger logger = LogManager.getLogger(PullRequestAnalyzer.class);

	public PullRequestAnalyzer(Path workingDirectory, Forge forge, CommitAnalyzer commitAnalyzer) {
    this.workingDirectory = Objects.requireNonNull(workingDirectory);
		this.forge = Objects.requireNonNull(forge);
		this.commitAnalyzer = Objects.requireNonNull(commitAnalyzer);
	}

	public PullRequestAnalysisResult analyze(PullRequest pr, MaracasOptions options) {
		Objects.requireNonNull(pr);
		Objects.requireNonNull(options);

		// Configuring Maracas according to BreakBot
		BreakbotConfig config = forge.fetchBreakbotConfig(pr.repository());
		config.excludes().forEach(excl -> options.getJApiOptions().addExcludeFromArgument(japicmp.util.Optional.of(excl), false));

		// For every package in mergeBase that may be impacted by the PR
		Commit v1 = pr.mergeBase();
		CommitBuilder builderV1 = makeBuilderForLibrary(pr, v1, Path.of(""), config.build());
		List<BuildModule> impactedPackages = inferImpactedPackages(pr, builderV1, options);
		logger.info("{} impacts {} packages: {}", pr, impactedPackages.size(), impactedPackages);

		// We need to run the whole analysis for each impacted package in the PR
		return new PullRequestAnalysisResult(
			pr,
			impactedPackages.stream().collect(Collectors.toMap(
				BuildModule::name,
				pkg -> analyzePackage(pr, pkg, config.build(), options)
			)),
			builderV1.getClonePath()
		);
	}

	private PackageAnalysisResult analyzePackage(PullRequest pr, BuildModule pkg, BreakbotConfig.Build buildConfig, MaracasOptions options) {
		try {
			logger.info("[{}] Now analyzing package {}", pr, pkg.name());

			Commit v1 = pr.mergeBase();
			Commit v2 = pr.head();

			// First, we compute the delta model to look for BCs
			CommitBuilder builderV1 = makeBuilderForLibrary(pr, v1, pkg.path(), buildConfig);
			CommitBuilder builderV2 = makeBuilderForLibrary(pr, v2, pkg.path(), buildConfig);
			Delta delta = commitAnalyzer.computeDelta(builderV1, builderV2, options);

			if (delta.getBreakingChanges().isEmpty())
				return PackageAnalysisResult.success(pkg.name(), delta, Collections.emptyMap());

			// If we find some, we fetch the appropriate clients and analyze the impact
			logger.info("Fetching clients for package {}", pkg.name());
			Collection<Commit> clients =
				forge.fetchAllClients(pr.repository(), pkg.name(), options.getClientsPerPackage(), options.getMinStarsPerClient())
					.stream()
					.map(repository -> forge.fetchCommit(repository, "HEAD"))
					.toList();
			logger.info("Found {} clients to analyze for {}", clients.size(), pkg.name());

			Map<Commit, CommitBuilder> builders = new HashMap<>();
			clients.forEach(c -> builders.put(c, makeBuilderForClient(pr, c)));

			AnalysisResult result = commitAnalyzer.computeImpact(delta, builders.values(), options);
			return PackageAnalysisResult.success(
				pkg.name(),
				delta,
				builders.keySet().stream().collect(Collectors.toMap(
					Commit::repository,
					c -> result.deltaImpacts().get(builders.get(c).getClonePath())
				))
			);
		} catch (Exception e) {
			return PackageAnalysisResult.failure(pkg.name(), e.getMessage());
		}
	}

	public List<BuildModule> inferImpactedPackages(PullRequest pr, CommitBuilder builder, MaracasOptions options) {
		builder.cloneCommit(options.getCloneTimeoutSeconds());
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
					logger.warn("Couldn't infer the impacted package for {}", f);

				return matchingModule;
			})
			.flatMap(Optional::stream)
			.distinct()
			.toList();
	}

	private CommitBuilder makeBuilderForLibrary(PullRequest pr, Commit c, Path module, BreakbotConfig.Build build) {
		BuildConfig buildConfig = new BuildConfig(module);
		build.goals().forEach(buildConfig::addGoal);
		build.properties().keySet().forEach(k -> buildConfig.setProperty(k, build.properties().get(k)));

		return new CommitBuilder(c, makeClonePath(pr, c), buildConfig);
	}

	private CommitBuilder makeBuilderForClient(PullRequest pr, Commit c) {
		return new CommitBuilder(c, makeClonePath(pr, c), BuildConfig.newDefault());
	}

  public Path makeClonePath(PullRequest pr, Commit c) {
    return workingDirectory
	    .resolve(pr.uid())
      .resolve(c.uid())
      .toAbsolutePath();
  }
}
