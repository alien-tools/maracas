package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.report.PullRequestReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public record PullRequestResponse(
	String message,
	LocalDateTime date,
	PullRequestDto pr,
	PullRequestReportDto report
) {
	public static PullRequestResponse status(PullRequest pr, String status) {
		return new PullRequestResponse(status, LocalDateTime.now(), pr != null ? PullRequestDto.of(pr) : null, null);
	}

	public static PullRequestResponse ok(PullRequest pr, PullRequestReport report) {
		return new PullRequestResponse("ok", LocalDateTime.now(), PullRequestDto.of(pr), PullRequestReportDto.of(report));
	}

	public static PullRequestResponse fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());
		return objectMapper.readValue(json, PullRequestResponse.class);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.writeValue(json, this);
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper.writeValueAsString(this);
	}
}
