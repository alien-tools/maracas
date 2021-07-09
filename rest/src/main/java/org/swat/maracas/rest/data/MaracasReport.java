package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

public record MaracasReport(
	Delta delta,
	Set<Detection> detections,
	Throwable error
) {
	public MaracasReport(Delta delta, Set<Detection> detections) {
		this(delta, detections, null);
	}

	public MaracasReport(Throwable error) {
		this(null, null, error);
	}

	public static MaracasReport fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, MaracasReport.class);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(json, this);
	}
}
