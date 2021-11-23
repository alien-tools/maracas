package com.github.maracas.rest.breakbot;

public record GithubRepositoryConfig(
	String owner,
	String repository,
	String sources,
	String branch,
	String sha
) {
	public GithubRepositoryConfig(String owner, String repository) {
		this(owner, repository, null, null, null);
	}
}
