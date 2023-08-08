package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;

import java.util.Collection;
import java.util.List;

public interface GitHubClientsFetcher {
	List<RepositoryModule> fetchModules(Repository repository);

	List<GitHubClient> fetchClients(RepositoryModule module, ClientFilter filter, int limit);

	default List<GitHubClient> fetchClients(Repository repository, ClientFilter filter, int limit) {
		return fetchModules(repository)
			.stream()
			.map(module -> fetchClients(module, filter, limit))
			.flatMap(Collection::stream)
			.toList();
	}

	@FunctionalInterface
	interface ClientFilter {
		ClientFilter ALL = client -> true;
		boolean evaluate(GitHubClient client);
	}
}
