package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GitHub's dependency graph holds information about a repository's dependencies/dependents.
 * Unfortunately, information about dependents isn't available through their REST nor GraphQL APIs,
 * so we have to scrap the https://github.com/org/repo/network/dependents webpage.
 * <br>
 * Note that repositories typically expose several "packages" (e.g., Maven modules) to which dependencies
 * are pointing.
 *
 * @see <a href="https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-the-dependency-graph">About the dependency graph</a>
 * @see <a href="https://docs.github.com/en/site-policy/acceptable-use-policies/github-acceptable-use-policies">GitHub Acceptable Use Policies</a>
 */
public class GitHubClientsFetcher {
	private final Repository repository;

	private static final String PACKAGES_URL = "https://github.com/%s/%s/network/dependents";
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
	private static final String REFERRER = "https://www.google.com";

	private static final Logger logger = LogManager.getLogger(GitHubClientsFetcher.class);

	public GitHubClientsFetcher(Repository repository) {
		this.repository = repository;
	}

	public List<String> fetchPackages() {
		try {
			Document pkgsPage = fetchPage(PACKAGES_URL.formatted(repository.owner(), repository.name()));

			if (pkgsPage != null) {
				Elements pkgsLinks = pkgsPage.select("#dependents .select-menu-item");
				List<Package> pkgs =
					pkgsLinks.stream()
						.map(link -> {
							String name = link.select(".select-menu-item-text").text().trim();
							String url = "https://github.com" + link.attr("href");
							return new Package(name, url, fetchClients(url));
						}).toList();

				System.out.println(pkgs);
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return Collections.emptyList();
	}

	public List<Client> fetchClients(String pkgUrl) {
		List<Client> clients = new ArrayList<>();

		try {
			Document pkgPage = fetchPage(pkgUrl);

			if (pkgPage != null) {
				List<String> clientRows = pkgPage.select("#dependents .Box-row").eachText();
				// rowText should be of the form "org / user stars forks"

				clientRows.forEach(row -> {
					String[] fields = row.split(" ");
					if (fields.length == 5) {
						clients.add(new Client(
							fields[0].trim(), fields[2].trim(),
							Integer.parseInt(fields[3].trim().replaceAll("[^0-9]", "")),
							Integer.parseInt(fields[4].trim().replaceAll("[^0-9]", "")));
					} else logger.error("Couldn't parse row {}", row);
				});

				Elements pagination = pkgPage.select("#dependents .paginate-container .BtnGroup-item");

				if (!pagination.isEmpty()) {
					Element nextBtn = pagination.get(1);
					String nextUrl = nextBtn.attr("abs:href");

					if (!nextUrl.isEmpty())
						clients.addAll(fetchClients(nextUrl));
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return clients;
	}

	private Document fetchPage(String url) throws IOException {
		try {
			logger.debug("Fetching {}", url);
			return Jsoup.connect(url).userAgent(USER_AGENT).referrer(REFERRER).get();
		} catch (HttpStatusException e) {
			if (e.getStatusCode() == 429) {
				logger.warn("Too many requests, sleeping...");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException ee) {
					logger.error(e);
					Thread.currentThread().interrupt();
				}
				return fetchPage(url);
			} else {
				logger.error("Couldn't fetch {}", url, e);
				throw e;
			}
		}
	}

	public record Package(String name, String url, List<Client> clients) {}
	public record Client(String owner, String name, int stars, int forks) {}
}
