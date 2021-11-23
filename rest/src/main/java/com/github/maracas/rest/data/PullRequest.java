package com.github.maracas.rest.data;

public record PullRequest(
		String owner,
		String repository,
		int id
) {
}
