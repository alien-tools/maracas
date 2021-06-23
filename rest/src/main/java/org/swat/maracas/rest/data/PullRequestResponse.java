package org.swat.maracas.rest.data;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PullRequestResponse {
	private final String message;
	private final Delta delta;

	public PullRequestResponse(String message, Delta delta) {
		this.message = message;
		this.delta = delta;
	}

	public String getMessage() {
		return message;
	}

	public Delta getDelta() {
		return delta;
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this);
	}
}
