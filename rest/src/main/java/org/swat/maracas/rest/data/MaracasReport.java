package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;

import org.swat.maracas.spoon.VersionAnalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MaracasReport {
	private VersionAnalyzer analyzer;
	private Throwable error;

	public MaracasReport(VersionAnalyzer analyzer) {
		this.analyzer = analyzer;
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
