package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;
import com.google.common.collect.Multimap;

public record MaracasResult(
	Delta delta,
	Multimap<Path, Detection> detections
) {
	public Collection<Detection> getAllDetections() {
		return detections.values();
	}

	public Collection<Detection> getDetectionsForClient(Path client) {
		return detections.get(client);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(delta);
		sb.append("\n");
		detections.keys().forEach(client -> {
			Collection<Detection> ds = detections.get(client);
			sb.append("For " + client);
			sb.append("\n");
			ds.forEach(d -> sb.append("\t" + d + "\n"));
		});
		return sb.toString();
	}
}
