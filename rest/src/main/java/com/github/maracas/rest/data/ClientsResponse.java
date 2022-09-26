package com.github.maracas.rest.data;

import com.github.maracas.forges.github.GitHubClientsFetcher;

import java.util.List;

public record ClientsResponse(
	String owner,
	String name,
	List<GitHubClientsFetcher.Package> packages,
	List<GitHubClientsFetcher.Client> clients
) {

}
