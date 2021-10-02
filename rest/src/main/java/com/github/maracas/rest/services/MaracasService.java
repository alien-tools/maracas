package com.github.maracas.rest.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.maracas.Maracas;
import com.github.maracas.delta.Delta;
import com.github.maracas.detection.Detection;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.ClientDetections;
import com.github.maracas.rest.data.MaracasReport;
import com.google.common.base.Stopwatch;

@Service
public class MaracasService {
	@Autowired
	private GithubService githubService;

	private static final Logger logger = LogManager.getLogger(MaracasService.class);

	public Delta makeDelta(Path jar1, Path jar2, Path sources) {
		logger.info("Computing Δ({} -> {})", jar1.getFileName(), jar2.getFileName());

		Stopwatch watch = Stopwatch.createStarted();
		Delta delta = Maracas.computeDelta(jar1, jar2);
		delta.populateLocations(sources);

		logger.info("Done Δ({} -> {}) in {}ms", jar1.getFileName(), jar2.getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return delta;
	}

	public List<Detection> makeDetections(Delta delta, Path clientSources) {
		logger.info("Computing detections({}, Δ({} -> {}))", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName());

		Stopwatch watch = Stopwatch.createStarted();
		List<Detection> detections = Maracas.computeDetections(clientSources, delta);

		logger.info("Done detections({}, Δ({} -> {})) in {}ms", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return detections;
	}

	public MaracasReport makeReport(String repository, String ref, Path basePath, Path jar1, Path jar2, BreakbotConfig config) {
		// Compute delta model
		Path sources = findSourceDirectory(basePath, null);
		Delta maracasDelta = makeDelta(jar1, jar2, sources);

		// Compute detections per client
		List<ClientDetections> detections = new ArrayList<>();
		config.getClients().parallelStream().forEach(c -> {
			try {
				// Clone the client
				String branch = githubService.getBranch(c);
				Path clientPath = githubService.cloneRepository(c);
				Path clientSources = findSourceDirectory(clientPath, c.sources());

				detections.add(
					new ClientDetections(c.repository(),
						makeDetections(maracasDelta, clientSources).stream()
							.map(d -> com.github.maracas.rest.data.Detection.fromMaracasDetection(d, c.repository(), branch, clientPath.toAbsolutePath().toString()))
							.toList())
				);

				logger.info("Done computing detections on {}", c.repository());
			} catch (Exception e) {
				logger.error(e);
				detections.add(new ClientDetections(c.repository(),
					new MaracasException("Couldn't analyze client " + c.repository(), e)));
			}
		});

		return new MaracasReport(
			com.github.maracas.rest.data.Delta.fromMaracasDelta(
				maracasDelta,
				repository,
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
