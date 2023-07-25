package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
public class GitHubClientsFetcher {
	private static final String MODULES_URL = "https://github.com/%s/%s/network/dependents";
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
	private static final String REFERRER = "https://www.google.com";
	private static final int HTTP_OK = 200;
	private static final int HTTP_TOO_MANY_REQUESTS = 429;
	private static final int FETCH_WAIT_TIME = 30;

	private static final Logger logger = LogManager.getLogger(GitHubClientsFetcher.class);

	public List<RepositoryModule> fetchModules(Repository repository) {
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

	public List<GitHubClient> fetchClients(Repository repository, int limit) {
		return fetchModules(repository)
			.stream()
			.map(module -> fetchClients(module, module.url(), limit))
			.flatMap(Collection::stream)
			.toList();
	}

	public List<GitHubClient> fetchClients(Repository repository) {
		return fetchClients(repository, Integer.MAX_VALUE);
	}

	public List<GitHubClient> fetchClients(RepositoryModule module, int limit) {
		return module != null && module.id() != null
			? fetchModules(module.repository()).stream()
					.filter(m -> m.id().equals(module.id()))
					.findFirst()
					.map(m -> fetchClients(m, m.url(), limit))
					.orElse(Collections.emptyList())
			: Collections.emptyList();
	}

	public List<GitHubClient> fetchClients(RepositoryModule module) {
		return fetchClients(module, Integer.MAX_VALUE);
	}

	private List<GitHubClient> fetchClients(RepositoryModule module, String url, int limit) {
		List<GitHubClient> clients = new ArrayList<>();
		Document modulePage = fetchPage(url);

		if (modulePage != null) {
			List<String> clientRows = modulePage.select("#dependents .Box-row").eachText();

			// row should be of the form "org / user stars forks"
			clientRows.forEach(row -> {
				String[] fields = row.split(" ");
				if (fields.length == 5) {
					clients.add(new GitHubClient(
						fields[0].trim(),
						fields[2].trim(),
						Integer.parseInt(fields[3].trim().replaceAll("\\D", "")),
						Integer.parseInt(fields[4].trim().replaceAll("\\D", "")),
						module));
				} else logger.error("Couldn't parse row {}", row);
			});

			int remaining = limit - clients.size();

			if (remaining > 0) {
				// Pagination should always be two Previous/Next button, one of them hidden in the first/last page
				Elements pagination = modulePage.select("#dependents .paginate-container .BtnGroup-item");
				if (pagination.size() == 2) {
					Element nextBtn = pagination.get(1);
					String nextUrl = nextBtn.attr("abs:href");

					if (!nextUrl.isEmpty())
						clients.addAll(fetchClients(module, nextUrl, remaining));
				}
			}
		}

		return clients.subList(0, Math.min(Math.max(0, limit), clients.size()));
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
}
