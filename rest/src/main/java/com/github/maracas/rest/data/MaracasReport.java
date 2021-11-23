package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public record MaracasReport(
	Delta delta,
	List<ClientDetections> clientDetections
) {
	public Collection<Detection> allDetections() {
		return
			clientDetections.stream()
				.map(ClientDetections::getDetections)
				.flatMap(Collection::stream)
				.toList();
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
