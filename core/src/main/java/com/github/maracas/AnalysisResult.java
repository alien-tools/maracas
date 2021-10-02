package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;
import com.google.common.collect.Multimap;

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
	Multimap<Path, Detection> detections
) {
	/**
	 * Returns all detections for all clients
	 *
	 * @return a collection of all detections
	 */
	public Collection<Detection> allDetections() {
		return detections.values();
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
