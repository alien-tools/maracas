package org.swat.maracas.rest.breakbot;

public record GithubClientConfig(
	String repository,
	String sources
) {
	public GithubClientConfig(String repository) {
		this(repository, null);
	}
}
