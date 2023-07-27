package com.github.maracas.forges.github;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * GitHub's dependency graph holds information about a repository's dependencies/dependents.
 * Unfortunately, information about dependents isn't available through their REST nor GraphQL APIs,
 * so we have to scrap the 'github.com/org/repo/network/dependents' webpage.
 * <br>
 * Note that repositories typically expose several modules ("packages" in GitHub's terminology) to which dependencies
 * are pointing.
 *
 * @see <a href="https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-the-dependency-graph">About the dependency graph</a>
 * @see <a href="https://docs.github.com/en/site-policy/acceptable-use-policies/github-acceptable-use-policies">GitHub Acceptable Use Policies</a>
 */
public class GitHubClientsScraper implements GitHubClientsFetcher {
	private final Path cacheDirectory;
	private final Duration cacheExpirationPeriod;

	private static final String MODULES_URL = "https://github.com/%s/%s/network/dependents";
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
	private static final String REFERRER = "https://www.google.com";
	private static final int HTTP_OK = 200;
	private static final int HTTP_TOO_MANY_REQUESTS = 429;
	private static final int FETCH_WAIT_TIME = 30;

	private static final Logger logger = LogManager.getLogger(GitHubClientsScraper.class);

	public GitHubClientsScraper(Duration cacheExpirationPeriod) {
		if (cacheExpirationPeriod.toSeconds() < 1)
			throw new IllegalArgumentException("cacheExpirationPeriod < 1s");

		Path dir; // Can't safely double-assign the final field without a temporary variable :(
		try {
			dir = Files.createTempDirectory("maracas-clients");
		} catch (IOException e) {
			dir = Paths.get("maracas-clients");
		}
		this.cacheDirectory = dir;
		this.cacheExpirationPeriod = cacheExpirationPeriod;
	}

	@Override
	public List<RepositoryModule> fetchModules(Repository repository) {
		Objects.requireNonNull(repository);

		String modulesPageUrl = MODULES_URL.formatted(repository.owner(), repository.name());
		Document modulesPage = fetchPage(modulesPageUrl);

		if (modulesPage != null) {
			Elements modules = modulesPage.select("#dependents .select-menu-item");

			if (!modules.isEmpty()) { // This repository has >= 1 modules
				return
					modules.stream()
						.map(link -> {
							String name = link.select(".select-menu-item-text").text().trim();
							String url = "https://github.com" + link.attr("href");
							return new RepositoryModule(repository, name, url);
						}).toList();
			} else { // This repository does not have any module
				return List.of(new RepositoryModule(repository, "default_module", modulesPageUrl));
			}
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<GitHubClient> fetchClients(RepositoryModule module, ClientFilter filter, int limit) {
		Objects.requireNonNull(module);
		Objects.requireNonNull(filter);
		if (limit < 1)
			throw new IllegalArgumentException("limit < 1");

		if (hasClientsCache(module)) {
			List<GitHubClient> cachedClients = readClientsCache(module);
			List<GitHubClient> matchingClients =
				cachedClients.stream()
					.filter(filter::evaluate)
					.toList();
			if (matchingClients.size() >= limit)
				return matchingClients.subList(0, limit);
		}

		String moduleUrl = !StringUtils.isEmpty(module.url())
			? module.url()
			: fetchModules(module.repository())
					.stream()
					.filter(m -> m.id().equals(module.id()))
					.map(RepositoryModule::url)
					.findFirst()
					.orElse("");

		if (!moduleUrl.isEmpty()) {
			Stopwatch sw = Stopwatch.createStarted();
			List<GitHubClient> allClients = fetchClientsRec(module, moduleUrl, filter, limit);
			logger.info("Fetched {} total clients for {} in {}s",
				allClients::size, () -> module, () -> sw.elapsed().toSeconds());
			writeClientsCache(module, allClients);
			return allClients
				.stream()
				.filter(filter::evaluate)
				.limit(limit)
				.toList();
		} else {
			return Collections.emptyList();
		}
	}

	private List<GitHubClient> fetchClientsRec(RepositoryModule module, String url, ClientFilter filter, int limit) {
		List<GitHubClient> allClients = new ArrayList<>();
		List<GitHubClient> matchingClients = new ArrayList<>();

		Document modulePage = fetchPage(url);
		if (modulePage != null) {
			List<String> clientRows = modulePage.select("#dependents .Box-row").eachText();

			// row should be of the form "org / user stars forks"
			clientRows.forEach(row -> {
				String[] fields = row.split(" ");
				if (fields.length == 5) {
					GitHubClient client = new GitHubClient(
						fields[0].trim(),
						fields[2].trim(),
						Integer.parseInt(fields[3].trim().replaceAll("\\D", "")),
						Integer.parseInt(fields[4].trim().replaceAll("\\D", "")),
						module
					);

					if (filter.evaluate(client))
						matchingClients.add(client);
					allClients.add(client);
				} else logger.error("Couldn't parse row {}", row);
			});

			int remaining = limit - matchingClients.size();
			if (remaining > 0) {
				// Pagination should always be two Previous/Next button, one of them hidden in the first/last page
				Elements pagination = modulePage.select("#dependents .paginate-container .BtnGroup-item");
				if (pagination.size() == 2) {
					Element nextBtn = pagination.get(1);
					String nextUrl = nextBtn.attr("abs:href");

					if (!nextUrl.isEmpty())
						allClients.addAll(fetchClientsRec(module, nextUrl, filter, remaining));
				}
			}
		}

		return allClients;
	}

	private Document fetchPage(String url) {
		try {
			Connection.Response res =
				Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.referrer(REFERRER)
					.ignoreHttpErrors(true)
					.execute();

			if (res.statusCode() == HTTP_OK) {
				return res.parse();
			} else if (res.statusCode() == HTTP_TOO_MANY_REQUESTS) {
				String retryAfter = res.header("Retry-After");
				int waitTime = retryAfter != null ? Integer.parseInt(retryAfter) : FETCH_WAIT_TIME;
				logger.warn("Too many requests; retrying after {}s", waitTime);
				Thread.sleep(1_000L * waitTime);
				return fetchPage(url);
			} else {
				logger.error("Couldn't fetch {} [HTTP {}]", url, res.statusCode());
			}
		} catch (IOException e) {
			logger.error("Couldn't fetch {}", url, e);
		} catch (InterruptedException ee) {
			logger.error(ee);
			Thread.currentThread().interrupt();
		}

		return null;
	}

	private boolean hasClientsCache(RepositoryModule module) {
		Path cacheFile = clientsCacheFile(module);
		if (Files.exists(cacheFile)) {
			try {
				Instant lastModified = Files.getLastModifiedTime(cacheFile).toInstant();
				Duration sinceLastModified = Duration.between(lastModified, Instant.now());

				return sinceLastModified.minus(cacheExpirationPeriod).isNegative();
			} catch (IOException e) {
				// we can safely swallow this one
			}
		}

		return false;
	}

	private List<GitHubClient> readClientsCache(RepositoryModule module) {
		try {
			Path cacheFile = clientsCacheFile(module);
			List<GitHubClient> clients = new ObjectMapper().readValue(cacheFile.toFile(), new TypeReference<>(){});
			logger.info("Retrieved {} total clients from {}", clients::size, () -> cacheFile);
			return clients;
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private void writeClientsCache(RepositoryModule module, List<GitHubClient> clients) {
		try {
			Path cacheFile = clientsCacheFile(module);
			Path parent = cacheFile.getParent();
			if (parent != null)
				Files.createDirectories(parent);
			new ObjectMapper().writeValue(cacheFile.toFile(), clients);
			logger.info("Serialized {} clients for {} in {}", clients.size(), module, cacheFile);
		} catch (IOException e) {
			logger.error("Couldn't save clients cache for {}", module, e);
		}
	}

	private Path clientsCacheFile(RepositoryModule module) {
		return cacheDirectory
			.resolve(module.repository().owner())
			.resolve(module.repository().name())
			.resolve(module.id().replace(":", "_") + "-clients.json")
			.toAbsolutePath();
	}
}
