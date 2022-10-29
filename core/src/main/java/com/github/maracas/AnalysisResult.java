package com.github.maracas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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
	Map<Path, DeltaImpact> deltaImpacts,
	/*
		The error we may have got along the way
	 */
	String error
) {
	public static AnalysisResult success(Delta delta, Map<Path, DeltaImpact> deltaImpacts) {
		return new AnalysisResult(delta, deltaImpacts, null);
	}

	public static AnalysisResult failure(String message) {
		return new AnalysisResult(null, null, message);
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
			delta,
			clients.stream().collect(toMap(
				SourcesDirectory::getLocation,
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
	 * Returns the {@link DeltaImpact} for all broken clients
	 */
	public Set<DeltaImpact> brokenClients() {
		return deltaImpacts.values()
			.stream()
			.filter(i -> !i.getBrokenUses().isEmpty())
			.collect(toSet());
	}

	/**
	 * Returns the {@link DeltaImpact} model for a given client
	 *
	 * @param client client owning the expected {@link DeltaImpact} model
	 * @return {@link DeltaImpact} model of the given client, or null if it doesn't exist
	 */
	public DeltaImpact deltaImpactForClient(SourcesDirectory client) {
		return deltaImpacts.get(client);
	}

	public String toJson() throws JsonProcessingException {
		return new ObjectMapper()
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(json, this);
	}
}
