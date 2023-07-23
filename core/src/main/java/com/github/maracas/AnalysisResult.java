package com.github.maracas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * The result of analyzing an {@link AnalysisQuery} with Maracas.
 */
public record AnalysisResult(
	/*
	  The delta model between two versions of the library
	 */
	Delta delta,
	/*
	  The delta impact model per analyzed client
	 */
	Map<SourcesDirectory, DeltaImpact> deltaImpacts
) {
	public static AnalysisResult success(Delta delta, List<DeltaImpact> deltaImpacts) {
		return new AnalysisResult(
			Objects.requireNonNull(delta),
			Objects.requireNonNull(deltaImpacts)
				.stream()
				.collect(toUnmodifiableMap(
					DeltaImpact::client,
					Function.identity()
				))
		);
	}

	/**
	 * Creates an {@link AnalysisResult} where the delta doesn't impact any client
	 *
	 * @param delta   the computed delta model
	 * @param clients a collection of clients
	 * @return the newly-created {@link AnalysisResult}
	 */
	public static AnalysisResult noImpact(Delta delta, Collection<SourcesDirectory> clients) {
		return AnalysisResult.success(
			Objects.requireNonNull(delta),
			Objects.requireNonNull(clients)
				.stream()
				.map(client -> DeltaImpact.success(client, delta, emptySet()))
				.toList()
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
			.map(DeltaImpact::brokenUses)
			.flatMap(Collection::stream)
			.collect(toSet());
	}

	/**
	 * Returns the {@link DeltaImpact} for all broken clients
	 */
	public Set<DeltaImpact> brokenClients() {
		return deltaImpacts.values()
			.stream()
			.filter(i -> !i.brokenUses().isEmpty())
			.collect(toSet());
	}

	public String toJson() throws JsonProcessingException {
		return new ObjectMapper()
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this);
	}
}
