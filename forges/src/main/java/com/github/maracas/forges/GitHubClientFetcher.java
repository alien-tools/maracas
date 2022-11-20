package com.github.maracas.forges;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class GitHubClientFetcher implements ClientFetcher {
	private final Forge forge;
	private static final Logger logger = LogManager.getLogger(GitHubClientFetcher.class);

	public GitHubClientFetcher(Forge forge) {
		this.forge = Objects.requireNonNull(forge);
	}

	@Override
	public List<Commit> fetchClients(Repository repository, Package pkg, int maxClients, int minStars) {
		List<Commit> clients =
			forge.fetchTopStarredClients(repository, pkg.id(), maxClients, minStars)
				.stream()
				.map(client -> forge.fetchCommit(client, "HEAD"))
				.toList();
		logger.info("Found {} clients to analyze for {}", clients.size(), pkg.id());
		return clients;
	}
}
