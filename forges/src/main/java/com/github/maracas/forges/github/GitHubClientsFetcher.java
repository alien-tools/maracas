package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
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
import java.util.Optional;

/**
 * GitHub's dependency graph holds information about a repository's dependencies/dependents.
 * Unfortunately, information about dependents isn't available through their REST nor GraphQL APIs,
 * so we have to scrap the 'github.com/org/repo/network/dependents' webpage.
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
	private static final int HTTP_OK = 200;
	private static final int HTTP_TOO_MANY_REQUESTS = 429;
	private static final int FETCH_WAIT_TIME = 30;

	private static final Logger logger = LogManager.getLogger(GitHubClientsFetcher.class);

	public record Package(String name, String url) {}
	public record Client(Package pkg, String owner, String name, int stars, int forks) {}

	public GitHubClientsFetcher(Repository repository) {
		this.repository = repository;
	}

	public List<Package> fetchPackages() {
		Optional<Document> pkgsPage = fetchPage(PACKAGES_URL.formatted(repository.owner(), repository.name()));

		return pkgsPage.map(document -> document.select("#dependents .select-menu-item")
			.stream()
			.map(link -> {
				String name = link.select(".select-menu-item-text").text().trim();
				String url = "https://github.com" + link.attr("href");
				return new Package(name, url);
			})
			.toList()
		).orElse(Collections.emptyList());
	}

	private List<Client> fetchClients(Package pkg, String url) {
		List<Client> clients = new ArrayList<>();
		Optional<Document> pkgPage = fetchPage(url);

		if (pkgPage.isPresent()) {
			List<String> clientRows = pkgPage.get().select("#dependents .Box-row").eachText();

			// row should be of the form "org / user stars forks"
			clientRows.forEach(row -> {
				String[] fields = row.split(" ");
				if (fields.length == 5) {
					clients.add(new Client(
						pkg,
						fields[0].trim(), fields[2].trim(),
						Integer.parseInt(fields[3].trim().replaceAll("\\D", "")),
						Integer.parseInt(fields[4].trim().replaceAll("\\D", ""))));
				} else logger.error("Couldn't parse row {}", row);
			});

			// Pagination should always be two Previous/Next button, one of them hidden in the first/last page
			Elements pagination = pkgPage.get().select("#dependents .paginate-container .BtnGroup-item");
			if (pagination.size() == 2) {
				Element nextBtn = pagination.get(1);
				String nextUrl = nextBtn.attr("abs:href");

				if (!nextUrl.isEmpty())
					clients.addAll(fetchClients(pkg, nextUrl));
			}
		}

		return clients;
	}

	public List<Client> fetchClients() {
		return fetchPackages()
			.stream()
			.map(pkg -> fetchClients(pkg, pkg.url()))
			.flatMap(Collection::stream)
			.toList();
	}

	public List<Client> fetchClients(String pkg) {
		return StringUtils.isEmpty(pkg)
			? fetchClients()
			: fetchPackages().stream()
					.filter(p -> p.name().equals(pkg))
					.findFirst()
					.map(p -> fetchClients(p, p.url()))
					.orElse(Collections.emptyList());
	}

	private Optional<Document> fetchPage(String url) {
		try {
			Connection.Response res =
				Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.referrer(REFERRER)
					.ignoreHttpErrors(true)
					.execute();

			if (res.statusCode() == HTTP_OK) {
				return Optional.of(res.parse());
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

		return Optional.empty();
	}
}
