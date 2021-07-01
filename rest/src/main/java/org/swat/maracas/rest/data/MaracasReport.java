package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MaracasReport {
	private final Delta delta;
	private final Set<Detection> detections;
	private final Throwable error;

	public MaracasReport(Delta delta, Set<Detection> detections) {
		this.delta = delta;
		this.detections = detections;
		this.error = null;
	}

	public MaracasReport(Throwable error) {
		this.delta = null;
		this.detections = null;
		this.error = error;
	}

	public Delta getDelta() {
		return delta;
	}

	public Throwable getError() {
		return error;
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
