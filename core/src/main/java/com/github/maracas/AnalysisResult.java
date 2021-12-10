package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.github.maracas.brokenUse.BrokenUse;
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
	 * The set of broken uses per analyzed client
	 */
	// Guava's Multimap cannot associate an empty/null value to a key. So we're
	// stuck with good old java.util.Map to store empty collections for clients
	// that do not have any broken use.
	Map<Path, Collection<BrokenUse>> brokenUses
) {
	public AnalysisResult {
		Objects.requireNonNull(delta);
		Objects.requireNonNull(brokenUses);
	}

	/**
	 * Returns all broken uses for all clients
	 *
	 * @return a collection of all broken uses
	 */
	public Collection<BrokenUse> allBrokenUses() {
		return brokenUses.values().stream().flatMap(Collection::stream).toList();
	}

	/**
	 * Returns all broken uses for a particular client {@code client}
	 *
	 * @param client The client of interest, identified by the path to its source code
	 * @return a collection of all broken uses for client {@code client}
	 */
	public Collection<BrokenUse> brokenUsesForClient(Path client) {
		return brokenUses.get(client);
	}
}
