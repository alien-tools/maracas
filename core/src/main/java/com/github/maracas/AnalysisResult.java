package com.github.maracas;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * The result of analyzing an {@link AnalysisQuery} with Maracas.
 */
public record AnalysisResult(
	/**
	 * The delta model between two versions of the library
	 */
	Delta delta,
	/**
	 * The delta impact model per analyzed client
	 */
	Map<Path, DeltaImpact> deltaImpacts
) {
	public AnalysisResult {
		Objects.requireNonNull(delta);
		Objects.requireNonNull(deltaImpacts);
	}

	/**
	 * Creates an {@link AnalysisResult} where the delta doesn't impact any client
	 *
	 * @param delta the computed delta model
	 * @param clients a collection of Path-identified clients
	 * @return the newly-created {@link AnalysisResult}
	 */
	public static AnalysisResult noImpact(Delta delta, Collection<Path> clients) {
		return new AnalysisResult(
			delta,
			clients.stream().collect(toMap(
				c -> c,
				c -> new DeltaImpact(c, delta, emptySet()))
			)
		);
	}

	/**
	 * Returns all broken uses for all clients
	 *
	 * @return a set of all broken uses
	 */
	public Set<BrokenUse> allBrokenUses() {
		return deltaImpacts.values()
			.stream()
			.map(DeltaImpact::getBrokenUses)
			.flatMap(Collection::stream)
			.collect(toSet());
	}

	/**
	 * Returns a {@link DeltaImpact} model given a client path.
	 *
	 * @param client client owning the expected {@link DeltaImpact} model
	 * @return {@link DeltaImpact} model of the given client, or null if it doesn't exist
	 */
	public DeltaImpact deltaImpactForClient(Path client) {
		return deltaImpacts.get(client.toAbsolutePath());
	}
}
