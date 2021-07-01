package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;

import org.swat.maracas.spoon.MaracasAnalysis;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MaracasReport {
	private MaracasAnalysis analysis;
	private Throwable error;

	public MaracasReport(MaracasAnalysis analysis) {
		this.analysis = analysis;
	}

	public MaracasReport(Throwable error) {
		this.error = error;
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
