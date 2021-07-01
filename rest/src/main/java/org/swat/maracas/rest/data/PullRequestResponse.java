package org.swat.maracas.rest.data;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PullRequestResponse {
	private final String message;
	private final MaracasReport report;

	public PullRequestResponse(String message, MaracasReport report) {
		this.message = message;
		this.report = report;
	}

	public String getMessage() {
		return message;
	}

	public MaracasReport getReport() {
		return report;
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this);
	}
}
