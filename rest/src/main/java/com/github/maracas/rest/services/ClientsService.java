package com.github.maracas.rest.services;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubClientsFetcher;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ClientsService {
	private static final Logger logger = LogManager.getLogger(ClientsService.class);

	public List<GitHubClientsFetcher.Client> fetchClients(Repository repository) {
		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Client> clients = fetcher.fetchClients();
		logger.info("Fetched {} total clients for {} in {}s", clients.size(), repository, sw.elapsed().toSeconds());
		return clients;
	}

	public List<GitHubClientsFetcher.Package> fetchPackages(Repository repository) {
		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Package> packages = fetcher.fetchPackages();
		logger.info("Fetched {} total packages for {} in {}s", packages.size(), repository, sw.elapsed().toSeconds());
		return packages;
	}

	public List<BreakbotConfig.GitHubRepository> buildClientsList(Repository repository, BreakbotConfig.Clients config) {
		List<BreakbotConfig.GitHubRepository> allClients = new ArrayList<>(config.repositories());

		if (config.top() > 0) {
			List<BreakbotConfig.GitHubRepository> topClients =
				fetchClients(repository).stream()
					.sorted(Comparator.comparingInt(GitHubClientsFetcher.Client::stars))
					.limit(config.top())
					.map(repo ->
						new BreakbotConfig.GitHubRepository(String.format("%s/%s", repo.owner(), repo.name()), null, null, null)
					)
					.toList();

			allClients.addAll(topClients);
		} else if (config.stars() > 0) {
			List<BreakbotConfig.GitHubRepository> starsClients =
				fetchClients(repository).stream()
					.filter(repo -> repo.stars() >= config.stars())
					.map(repo ->
						new BreakbotConfig.GitHubRepository(String.format("%s/%s", repo.owner(), repo.name()), null, null, null)
					)
					.toList();

			allClients.addAll(starsClients);
		}

		return allClients;
	}
}
