package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.github.maracas.delta.Delta;
import com.github.maracas.detection.Detection;

/**
 * The result of analyzing an {@link AnalysisQuery} with Maracas.
 */
public record AnalysisResult(
	/**
	 * The delta model between two versions of the library
	 */
	Delta delta,
	/**
	 * The set of detections per analyzed client
	 */
	// Guava's Multimap cannot associate an empty/null value to a key. So we're
	// stuck with good old java.util.Map to store empty collections for clients
	// that do not have any detection.
	Map<Path, Collection<Detection>> detections
) {
	public AnalysisResult {
		Objects.requireNonNull(delta);
		Objects.requireNonNull(detections);
	}

	/**
	 * Returns all detections for all clients
	 *
	 * @return a collection of all detections
	 */
	public Collection<Detection> allDetections() {
		return detections.values().stream().flatMap(Collection::stream).toList();
	}

	/**
	 * Returns all detections for a particular client {@code client}
	 *
	 * @param client The client of interest, identified by the path to its source code
	 * @return a collection of all detections for client {@code client}
	 */
	public Collection<Detection> detectionsForClient(Path client) {
		return detections.get(client);
	}
}
