package com.github.maracas.rest.data;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public record PullRequestResponse(
	String message,
	MaracasReport report
) {
	public PullRequestResponse(String message) {
		this(message, null);
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this);
	}
}
