package com.github.maracas.rest.breakbot;

public record GithubRepositoryConfig(
	String repository,
	String sources,
	String branch,
	String sha
) {
	public GithubRepositoryConfig(String repository) {
		this(repository, null, null, null);
	}
}
