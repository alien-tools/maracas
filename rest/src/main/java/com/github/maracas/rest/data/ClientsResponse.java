package com.github.maracas.rest.data;

import com.github.maracas.forges.github.GitHubClient;
import com.github.maracas.forges.github.GitHubModule;

import java.util.Collections;
import java.util.List;

public record ClientsResponse(
	String message,
	String owner,
	String name,
	List<GitHubModule> modules,
	List<GitHubClient> clients
) {
	public ClientsResponse(String message) {
		this(message, "", "", Collections.emptyList(), Collections.emptyList());
	}

	public ClientsResponse(String owner, String name, List<GitHubModule> modules, List<GitHubClient> clients) {
		this("ok", owner, name, modules, clients);
	}
}
