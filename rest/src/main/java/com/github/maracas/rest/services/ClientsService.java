package com.github.maracas.rest.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubClientsFetcher;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ClientsService {
	@Value("${maracas.client-path:./clients}")
	private String clientPath;
	@Value("${maracas.client-cache-expiration:7}")
	private int clientsCacheExpiration;

	private static final Logger logger = LogManager.getLogger(ClientsService.class);

	@PostConstruct
	public void initialize() {
		Path.of(clientPath).toFile().mkdirs();
	}

	public List<GitHubClientsFetcher.Client> fetchClients(Repository repository, String packageId) {
		File cacheFile = cacheFile(repository, packageId);
		ObjectMapper objectMapper = new ObjectMapper();

		if (cacheIsValid(cacheFile)) {
			try {
				List<GitHubClientsFetcher.Client> clients = objectMapper.readValue(cacheFile, new TypeReference<>(){});
				logger.info("Fetched {} total clients for {} [package: {}] from {}",
					clients.size(), repository, packageId, cacheFile);
				return clients;
			} catch (IOException e) {
				logger.error(e);
			}
		}

		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Client> clients = fetcher.fetchClients(packageId);
		logger.info("Fetched {} total clients for {} [package: {}] in {}s",
			clients.size(), repository, packageId, sw.elapsed().toSeconds());

		try {
			cacheFile.getParentFile().mkdirs();
			objectMapper.writeValue(cacheFile, clients);
			logger.info("Serialized clients for {} [package: {}] in {}", repository, packageId, cacheFile);
		} catch (IOException e) {
			logger.error(e);
		}

		return clients;
	}

	public List<GitHubClientsFetcher.Package> fetchPackages(Repository repository) {
		Stopwatch sw = Stopwatch.createStarted();
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Package> packages = fetcher.fetchPackages();
		logger.info("Fetched {} total packages for {} in {}s", packages.size(), repository, sw.elapsed().toSeconds());
		return packages;
	}

	public List<BreakbotConfig.GitHubRepository> buildClientsList(Repository repository, BreakbotConfig.Clients config, String packageId) {
		List<BreakbotConfig.GitHubRepository> allClients = new ArrayList<>(config.repositories());

		if (config.top() > 0) {
			List<BreakbotConfig.GitHubRepository> topClients =
				fetchClients(repository, packageId).stream()
					.sorted(Comparator.comparingInt(GitHubClientsFetcher.Client::stars))
					.limit(config.top())
					.map(repo ->
						new BreakbotConfig.GitHubRepository(String.format("%s/%s", repo.owner(), repo.name()), null, null, null)
					)
					.toList();

			allClients.addAll(topClients);
		} else if (config.stars() > 0) {
			List<BreakbotConfig.GitHubRepository> starsClients =
				fetchClients(repository, packageId).stream()
					.filter(repo -> repo.stars() >= config.stars())
					.map(repo ->
						new BreakbotConfig.GitHubRepository(String.format("%s/%s", repo.owner(), repo.name()), null, null, null)
					)
					.toList();

			allClients.addAll(starsClients);
		}

		return allClients;
	}

	private boolean cacheIsValid(File cacheFile) {
		if (cacheFile.exists()) {
			Date modified = new Date(cacheFile.lastModified());
			Date now = Date.from(Instant.now());

			long daysDiff = TimeUnit.DAYS.convert(Math.abs(now.getTime() - modified.getTime()), TimeUnit.MILLISECONDS);
			return daysDiff <= clientsCacheExpiration;
		}

		return false;
	}

	private File cacheFile(Repository repository, String packageId) {
		return Path.of(clientPath)
			.resolve(repository.owner())
			.resolve(repository.name())
			.resolve(packageId + "-clients.json")
			.toAbsolutePath()
			.toFile();
	}
}
