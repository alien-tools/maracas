package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;
import com.google.common.collect.Multimap;

public record AnalysisResult(
	Delta delta,
	Multimap<Path, Detection> detections
) {
	public Collection<Detection> allDetections() {
		return detections.values();
	}

	public Collection<Detection> detectionsForClient(Path client) {
		return detections.get(client);
	}
}
