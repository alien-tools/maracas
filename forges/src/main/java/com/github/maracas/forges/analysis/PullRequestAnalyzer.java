package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.ClientFetcher;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.build.CommitBuilderFactory;
import com.github.maracas.forges.Package;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.PackageReport;
import com.github.maracas.forges.report.PullRequestReport;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PullRequestAnalyzer {
	private final Path workingDirectory;
	private final ClientFetcher clientFetcher;
	private final CommitBuilderFactory commitBuilderFactory;
	private final CommitAnalyzer commitAnalyzer;

	private static final Logger logger = LogManager.getLogger(PullRequestAnalyzer.class);

	public PullRequestAnalyzer(Path workingDirectory, CommitBuilderFactory commitBuilderFactory, ClientFetcher clientFetcher, CommitAnalyzer commitAnalyzer) {
		this.workingDirectory = Objects.requireNonNull(workingDirectory);
		this.clientFetcher = Objects.requireNonNull(clientFetcher);
		this.commitBuilderFactory = Objects.requireNonNull(commitBuilderFactory);
		this.commitAnalyzer = Objects.requireNonNull(commitAnalyzer);
	}

	public PullRequestReport analyze(PullRequest pr, MaracasOptions options) {
		Objects.requireNonNull(pr);
		Objects.requireNonNull(options);

		logger.info("Now analyzing {}", pr);

		// If there's nothing interesting, skip
		if (pr.changedJavaFiles().isEmpty())
			return PullRequestReport.success(pr, Collections.emptyList());

		// Pick the versions we're interested in: mergeBase vs head
		Commit v1 = pr.mergeBase();
		Commit v2 = pr.head();
		CommitBuilder builderV1 = commitBuilderFactory.createLibraryBuilder(v1, newClonePath(v1), BuildConfig.newDefault());
		CommitBuilder builderV2 = commitBuilderFactory.createLibraryBuilder(v2, newClonePath(v2), BuildConfig.newDefault());

		try {
			// For every package in v1 that's updated by the PR's changes
			builderV1.cloneCommit(options.getCloneTimeoutSeconds());
			List<Package> packages = builderV1.locatePackages();
			List<Package> updatedPackages = inferUpdatedPackages(packages, pr.changedJavaFiles());
			logger.info("{} impacts {}/{} packages: {}", pr,
				updatedPackages.size(), packages.size(), updatedPackages);

			// We need to run the whole analysis for each updated package
			List<PackageReport> packageReports = updatedPackages.stream()
				.map(pkg -> analyzePackage(pr, pkg, builderV1, builderV2, options))
				.toList();

			return PullRequestReport.success(pr, packageReports);
		} catch (Exception e) {
			return PullRequestReport.error(pr, e.getMessage());
		}
	}

	private PackageReport analyzePackage(PullRequest pr, Package pkg, CommitBuilder v1, CommitBuilder v2, MaracasOptions options) {
		try {
			logger.info("Now analyzing {}", pkg);

			// First, we compute the delta model to look for BCs
			CommitBuilder pkgBuilderV1 = commitBuilderFactory.createLibraryBuilder(v1.getCommit(), v1.getClonePath(), new BuildConfig(pkg.modulePath()));
			CommitBuilder pkgBuilderV2 = commitBuilderFactory.createLibraryBuilder(v2.getCommit(), v2.getClonePath(), new BuildConfig(pkg.modulePath()));
			Delta delta = commitAnalyzer.computeDelta(pkgBuilderV1, pkgBuilderV2, options);

			// If no BC, we stop here
			if (delta.getBreakingChanges().isEmpty())
				return PackageReport.success(pkg, delta, Collections.emptyList(), pr, pkgBuilderV1.getClonePath());

			// Otherwise we look at clients
			List<Commit> clients = clientFetcher.fetchClients(pr.repository(), pkg, options.getClientsPerPackage(), options.getMinStarsPerClient());
			List<CommitBuilder> clientBuilders =
				clients.stream()
					.map(c -> commitBuilderFactory.createClientBuilder(c, newClonePath(c), BuildConfig.newDefault()))
					.toList();
			List<ClientImpact> clientsImpact = commitAnalyzer.computeImpact(delta, clientBuilders, options);
			return PackageReport.success(pkg, delta, clientsImpact, pr, pkgBuilderV1.getClonePath());
		} catch (Exception e) {
			return PackageReport.error(pkg, e.getMessage());
		}
	}

	private List<Package> inferUpdatedPackages(List<Package> packages, List<Path> files) {
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
