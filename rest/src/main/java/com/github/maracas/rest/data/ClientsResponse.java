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
	public static ClientsResponse status(String message) {
		return new ClientsResponse(message, null, null, Collections.emptyList(), Collections.emptyList());
	}

	public static ClientsResponse ok(String owner, String name, List<GitHubClientsFetcher.Package> packages, List<GitHubClientsFetcher.Client> clients) {
		return new ClientsResponse("ok", owner, name, packages, clients);
	}
}
