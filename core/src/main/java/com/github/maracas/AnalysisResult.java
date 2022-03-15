package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.brokenUse.DeltaImpact;
import com.github.maracas.delta.Delta;

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
	 * Returns all broken uses for all clients
	 *
	 * @return a set of all broken uses
	 */
	public Set<BrokenUse> allBrokenUses() {
		return deltaImpacts.values()
		    .stream()
		    .map(m -> m.getBrokenUses())
		    .flatMap(Collection::stream)
		    .collect(Collectors.toSet());
	}

	/**
	 * Returns all broken uses for a particular client {@code client}
	 * @deprecated as of 0.2.0-SNAPSHOT, replaced by {@link #deltaImpactForClient(Path)}.
	 *             Access the {@link BrokenUse} instances of the {@link DeltaImpact}
	 *             model via the {@link DeltaImpact#getBrokenUses()} method.
	 *
	 * @param client The client of interest, identified by the path to its source code
	 * @return a collection of all broken uses for client {@code client}
	 */
	@Deprecated
	public Set<BrokenUse> brokenUsesForClient(Path client) {
		return deltaImpacts.get(client).getBrokenUses();
	}

	/**
	 * Returns a {@link DeltaImpact} model given a client path.
	 *
	 * @param client client owning the expected {@link DeltaImpact} model
	 * @return {@link DeltaImpact} model of the given client
	 */
	public DeltaImpact deltaImpactForClient(Path client) {
	    return deltaImpacts.get(client);
	}
}
