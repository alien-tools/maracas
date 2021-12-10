package com.github.maracas.rest.services;

import com.github.maracas.Maracas;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.Delta;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequest;
import com.google.common.base.Stopwatch;
import japicmp.config.Options;
import japicmp.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MaracasService {
	@Autowired
	private GitHubService githubService;

	private static final Logger logger = LogManager.getLogger(MaracasService.class);

	public Delta makeDelta(Path jar1, Path jar2, Path sources, BreakbotConfig config) {
		logger.info("Computing Δ({} -> {})", jar1.getFileName(), jar2.getFileName());

		Options japiOptions = Maracas.defaultJApiOptions();
		config.excludes().forEach(excl -> japiOptions.addExcludeFromArgument(Optional.of(excl), false));

		Stopwatch watch = Stopwatch.createStarted();
		Delta delta = Maracas.computeDelta(jar1, jar2, japiOptions);
		delta.populateLocations(sources);

		logger.info("Done Δ({} -> {}) in {}ms", jar1.getFileName(), jar2.getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return delta;
	}

	public Collection<BrokenUse> makeDetections(Delta delta, Path clientSources) {
		logger.info("Computing detections({}, Δ({} -> {}))", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName());

		Stopwatch watch = Stopwatch.createStarted();
		Collection<BrokenUse> detections = Maracas.computeBrokenUses(clientSources, delta);

		logger.info("Done detections({}, Δ({} -> {})) in {}ms", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return detections;
	}

	public MaracasReport makeReport(PullRequest pr, String ref, Path basePath, Path jar1, Path jar2, BreakbotConfig config) {
		// Compute delta model
		Path sources = findSourceDirectory(basePath, null);
		Delta maracasDelta = makeDelta(jar1, jar2, sources, config);

		// Compute detections per client
		List<ClientReport> detections = new ArrayList<>();

		// No need to compute anything if there's no BC
		if (maracasDelta.getBreakingChanges().isEmpty())
			detections.addAll(
				config.clients().stream()
					.map(c -> ClientReport.empty(c.repository()))
					.toList()
			);
		else
			config.clients().parallelStream().forEach(c -> {
				try {
					// Clone the client
					String branch = githubService.getBranchName(c);
					Path clientPath = githubService.cloneRepository(c);
					Path clientSources = findSourceDirectory(clientPath, c.sources());
					String[] fields = c.repository().split("/");

					detections.add(
						ClientReport.success(c.repository(),
							makeDetections(maracasDelta, clientSources).stream()
								.map(d -> com.github.maracas.rest.data.Detection.fromMaracasDetection(d, fields[0], fields[1],
										branch, clientPath.toAbsolutePath().toString()))
								.toList())
					);

					logger.info("Done computing detections on {}", c.repository());
				} catch (Exception e) {
					logger.error(e);
					detections.add(ClientReport.error(c.repository(),
						new MaracasException("Couldn't analyze client " + c.repository(), e)));
				}
			});

		return new MaracasReport(
			com.github.maracas.rest.data.Delta.fromMaracasDelta(
				maracasDelta,
				pr,
				ref,
				basePath.toAbsolutePath().toString()
			),
			detections
		);
	}

	protected Path findSourceDirectory(Path basePath, String configSources) {
		Path sources = basePath;
		// User-defined config
		if (configSources != null && !configSources.isEmpty()
			&& basePath.resolve(configSources).toFile().exists())
			sources = basePath.resolve(configSources);
		// Or just try "standard" paths
		else if (basePath.resolve("src/main/java").toFile().exists())
			sources = basePath.resolve("src/main/java");
		else if (basePath.resolve("src/").toFile().exists())
			sources = basePath.resolve("src/");

		return sources;
	}
}
