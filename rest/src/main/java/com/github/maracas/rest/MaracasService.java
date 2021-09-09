package com.github.maracas.rest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.maracas.ClientAnalyzer;
import com.github.maracas.VersionAnalyzer;
import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.ClientDetections;
import com.github.maracas.rest.data.MaracasReport;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Service
public class MaracasService {
	@Autowired
	private GitHub github;
	@Autowired
	private GithubService githubService;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;

	private static final Logger logger = LogManager.getLogger(MaracasService.class);

	public Delta makeDelta(Path jar1, Path jar2, Path sources) {
		logger.info("Computing delta {} -> {}", jar1, jar2);

		VersionAnalyzer analyzer = new VersionAnalyzer(jar1, jar2);
		analyzer.computeDelta();
		analyzer.populateLocations(sources);
		return analyzer.getDelta();
	}

	public List<Detection> makeDetections(Delta delta, Path clientSources) {
		logger.info("Computing detections on client {}", clientSources);
		ClientAnalyzer clientAnalyzer = new ClientAnalyzer(delta, clientSources, delta.getV1());
		clientAnalyzer.computeDetections();
		return clientAnalyzer.getDetections();
	}

	public MaracasReport makeReport(String repository, Path basePath, Path jar1, Path jar2, BreakbotConfig config) {
		// Compute delta model
		Delta maracasDelta = makeDelta(jar1, jar2, basePath);

		// Compute detections per client
		Multimap<String, com.github.maracas.rest.data.Detection> detections = ArrayListMultimap.create();
		config.getClients().parallelStream().forEach(c -> {
			try {
				// Clone the client
				logger.info("Cloning client {}", c.repository());
				GHRepository clientRepo = github.getRepository(c.repository());
				String clientBranch = clientRepo.getDefaultBranch();
				Path clientPath = Paths.get(clonePath)
					.resolve(clientRepo.getOwnerName())
					.resolve(clientRepo.getBranch(clientBranch).getSHA1());

				githubService.cloneRemote(clientRepo.getHttpTransportUrl(), clientBranch, clientPath);

				Path clientSources = clientPath;
				// From .breakbot.yml
				if (c.sources() != null && !c.sources().isEmpty())
					clientSources = clientPath.resolve(c.sources());
				// Or just try "standard" paths
				else if (clientPath.resolve("src/main/java").toFile().exists())
					clientSources = clientPath.resolve("src/main/java");
				else if (clientPath.resolve("src/").toFile().exists())
					clientSources = clientPath.resolve("src/");
				else {
					logger.warn("Couldn't find src path in {}, defaulting to {}", clientPath, clientSources);
				}

				detections.putAll(c.repository(),
					makeDetections(maracasDelta, clientSources).stream()
						.map(d -> com.github.maracas.rest.data.Detection.fromMaracasDetection(d, c.repository(), clientPath.toAbsolutePath().toString()))
						.collect(Collectors.toList()));
			} catch (IOException e) {
				logger.error(e);
			}
		});

		return new MaracasReport(
			com.github.maracas.rest.data.Delta.fromMaracasDelta(
				maracasDelta,
				repository,
				basePath.toAbsolutePath().toString()
			),
			detections.keySet().stream()
				.map(repo -> new ClientDetections(repo, new ArrayList<>(detections.get(repo))))
				.toList()
		);
	}
}
