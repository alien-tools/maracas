package com.github.maracas.rest.services;

import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.brokenUse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.google.common.base.Stopwatch;
import japicmp.config.Options;
import japicmp.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class MaracasService {
	private static final Logger logger = LogManager.getLogger(MaracasService.class);

	public Delta makeDelta(Path jar1, Path jar2, Path sources, BreakbotConfig config) {
		logger.info("Computing Δ({} -> {})", jar1.getFileName(), jar2.getFileName());

		MaracasOptions options = MaracasOptions.newDefault();
		Options jApiOptions = options.getJApiOptions();
		config.excludes().forEach(excl -> jApiOptions.addExcludeFromArgument(Optional.of(excl), false));

		Stopwatch watch = Stopwatch.createStarted();
		Delta delta = Maracas.computeDelta(jar1, jar2, options);
		delta.populateLocations(sources);

		logger.info("Done Δ({} -> {}) in {}ms", jar1.getFileName(), jar2.getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return delta;
	}

	public Set<BrokenUse> makeBrokenUses(Delta delta, Path clientSources) {
		logger.info("Computing brokenUses({}, Δ({} -> {}))", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName());

		Stopwatch watch = Stopwatch.createStarted();
		DeltaImpact deltaImpact = Maracas.computeDeltaImpact(clientSources, delta);
		Set<BrokenUse> brokenUses = deltaImpact.getBrokenUses();

		logger.info("Done brokenUses({}, Δ({} -> {})) in {}ms", clientSources,
			delta.getOldJar().getFileName(), delta.getNewJar().getFileName(),
			watch.elapsed(TimeUnit.MILLISECONDS));
		return brokenUses;
	}
}
