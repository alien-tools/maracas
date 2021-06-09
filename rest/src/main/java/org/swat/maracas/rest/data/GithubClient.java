package org.swat.maracas.rest.data;

public class GithubClient {
	private final String user;
	private final String repository;

	public GithubClient(String user, String repository) {
		this.user = user;
		this.repository = repository;
	}

	public String getUser() {
		return user;
	}

	public String getRepository() {
		return repository;
	}
}
