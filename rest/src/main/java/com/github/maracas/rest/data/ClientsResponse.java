package com.github.maracas.rest.data;

import com.github.maracas.forges.github.GitHubClientsFetcher;

import java.util.Collections;
import java.util.List;

public record ClientsResponse(
	String message,
	String owner,
	String name,
	List<GitHubClientsFetcher.Package> packages,
	List<GitHubClientsFetcher.Client> clients
) {
	public ClientsResponse(String message) {
		this(message, "", "", Collections.emptyList(), Collections.emptyList());
	}

	public ClientsResponse(String owner, String name, List<GitHubClientsFetcher.Package> packages, List<GitHubClientsFetcher.Client> clients) {
		this("ok", owner, name, packages, clients);
	}
}
