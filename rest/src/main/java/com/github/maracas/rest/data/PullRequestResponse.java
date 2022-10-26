package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.maracas.forges.PullRequest;

import java.io.IOException;
import java.time.LocalDateTime;

public record PullRequestResponse(
	PullRequest pr,
	String message,
	LocalDateTime date,
	MaracasReport report
) {
	public static PullRequestResponse status(PullRequest pr, String message) {
		return new PullRequestResponse(pr, message, LocalDateTime.now(), null);
	}

	public static PullRequestResponse ok(PullRequest pr, MaracasReport report) {
		return new PullRequestResponse(pr, "ok", LocalDateTime.now(), report);
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper.writeValueAsString(this);
	}
}
