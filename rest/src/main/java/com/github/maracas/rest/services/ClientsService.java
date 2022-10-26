package com.github.maracas.rest.services;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubClientsFetcher;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ClientsService {
	@Autowired
	private GitHub github;
	private GitHubForge forge;
	@Value("${maracas.client-path:./clients}")
	private String clientPath;
	@Value("${maracas.client-cache-expiration:7}")
	private int clientsCacheExpiration;

	private static final Logger logger = LogManager.getLogger(ClientsService.class);

	@PostConstruct
	public void initialize() {
		Path.of(clientPath).toFile().mkdirs();
		forge = new GitHubForge(github);
		forge.setClientsCacheExpirationDays(clientsCacheExpiration);
		forge.setClientsCacheDirectory(Path.of(clientPath));
	}

	public List<GitHubClientsFetcher.Package> fetchPackages(Repository repository) {
		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Package> packages = fetcher.fetchPackages();
		logger.info("Fetched {} total packages for {} in {}s", packages.size(), repository, sw.elapsed().toSeconds());
		return packages;
	}

	public List<GitHubClientsFetcher.Client> fetchClients(Repository repository) {
		Stopwatch sw = Stopwatch.createStarted();
		List<GitHubClientsFetcher.Package> packages = fetchPackages(repository);
		List<GitHubClientsFetcher.Client> clients =
			packages.stream()
				.map(pkg -> forge.fetchClients(repository, pkg.name()))
				.flatMap(Collection::stream)
				.toList();
		logger.info("Fetched {} total clients for {} in {}s", clients.size(), repository, sw.elapsed().toSeconds());
		return clients;
	}

	public List<BreakbotConfig.GitHubRepository> buildClientsList(Repository repository, BreakbotConfig.Clients config, String packageId) {
		List<BreakbotConfig.GitHubRepository> allClients = new ArrayList<>(config.repositories());


		// FIXME: Dirty hack for our XPs: we're always forking popular libraries to run BreakBot, so the clients list on
		// our side is always empty; if we detect a fork, we instead gather clients from the original repository
		Repository actualRepository = repository;
		try {
			GHRepository repo = github.getRepository(String.format("%s/%s", repository.owner(), repository.name()));
			if (repo != null && repo.getParent() != null) {
				actualRepository = forge.fetchRepository(repo.getParent().getOwnerName(), repo.getParent().getName());
			}
		} catch (IOException e) {
			logger.error(e);
		}

		if (config.top() > 0) {
			allClients.addAll(
				forge.fetchTopClients(actualRepository, packageId, config.top()).stream()
					.map(repo -> new BreakbotConfig.GitHubRepository(
						String.format("%s/%s", repo.owner(), repo.name()),
						null, null, null))
					.toList()
			);
		} else if (config.stars() > 0) {
			allClients.addAll(
				forge.fetchClients(actualRepository, packageId).stream()
					.map(repo -> new BreakbotConfig.GitHubRepository(
						String.format("%s/%s", repo.owner(), repo.name()),
						null, null, null))
					.toList()
			);
		}

		return allClients;
	}
}
