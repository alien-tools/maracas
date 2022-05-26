package com.github.maracas.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.utils.GitHubUtil;
import com.github.maracas.experiments.utils.Queries;
import com.github.maracas.experiments.utils.Util;

/**
 *
 */
public class FetchGitHubRepositories {
	public static final int REPO_MIN_STARS = 10;
	public static final int REPO_MAX_STARS = 9999999;
	public static final String REPO_LAST_PUSHED_DATE = "2022-02-12";
	public static final int PR_LAST_MERGED_IN_DAYS = 90;
	public static final int LAST_PUSH_IN_DAYS = 90;
	public static final String GITHUB_GRAPHQL = "https://api.github.com/graphql";
	private static final String GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN");

	/**
	 * List of GitHub repositories considered for the experiment
	 */
	private List<Repository> repositories;

	public FetchGitHubRepositories() {
		this.repositories = new ArrayList<Repository>();
	}

	public void run() {
		var repos = fetchRepositories(null, REPO_MIN_STARS, REPO_MAX_STARS);
		System.out.println("Found " + repos.size());

		try (FileWriter csv = new FileWriter("output.csv")) {
			csv.write("owner,name,stars,package,clients,mergedPrs,maven,gradle\n");
			csv.flush();

			for (var repo : repos) {
				var packages = fetchClientsPerPackage(repo);
				var total = packages.values().stream().reduce(0, Integer::sum);
				System.out.printf("%s has %d clients%n", repo, total);
				csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.getOwner(), repo.getName(), repo.getStars(), "total", total,
					repo.getMergedPRs(), repo.isMaven(), repo.isGradle()));

				for (var pkg : packages.entrySet()) {
					System.out.printf("\t%s: %s", pkg.getKey(), pkg.getValue());
					csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.getOwner(), repo.getName(), repo.getStars(), pkg.getKey(), pkg.getValue(),
						repo.getMergedPRs(), repo.isMaven(), repo.isGradle()));
				}

				csv.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Repository> fetchRepositories(String cursor, int minStars, int maxStars) {
		var cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";

		var query = Queries.GRAPHQL_LIBRARIES_QUERY.formatted(minStars, maxStars, REPO_LAST_PUSHED_DATE, cursorQuery);
		var response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

		var repos = new ArrayList<Repository>();
		try {
			var mapper = new ObjectMapper();
			var json = mapper.readTree(response.getBody());
			var search = json.get("data").get("search");
			var pageInfo = search.get("pageInfo");
			var hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			var endCursor = pageInfo.get("endCursor").asText();

			var nextStars = maxStars;
			for (var repoEdge: search.withArray("edges")) {
				var repoJson = repoEdge.get("node");
				var nameWithOwner = repoJson.get("nameWithOwner").asText();
				var fields = nameWithOwner.split("/");
				var owner = fields[0];
				var name = fields[1];
				var lastPush = repoJson.get("pushedAt").asText();
				var prs = repoJson.get("pullRequests");
				var lastPR = prs.findValuesAsText("mergedAt");
				var mergedPRs = prs.get("mergedPRs").asInt();
				var isMaven = repoJson.get("pom").hasNonNull("oid");
				var isGradle = repoJson.get("gradle").hasNonNull("oid");
				var stars = repoJson.get("stargazerCount").asInt();

				Repository repo = new Repository(owner, name, stars, lastPush, mergedPRs, isMaven, isGradle);
				repo.setPullRequests(extractPRs(prs));

				if (stars < nextStars)
					nextStars = stars;

				if (!lastPR.isEmpty() && (isMaven || isGradle)) {
					var lastMerged = Util.stringToLocalDate(lastPR.get(0));
					var now = LocalDate.now();
					var between = Duration.between(lastMerged.atStartOfDay(), now.atStartOfDay());

					if (between.toDays() <= PR_LAST_MERGED_IN_DAYS)
						repos.add(repo);
					else
						System.out.printf("%s last PR merged on %s, skipping.%n", repo, lastMerged);
				}
			}

			if (hasNextPage)
				repos.addAll(fetchRepositories(endCursor, minStars, maxStars));
			else if (nextStars > minStars)
				repos.addAll(fetchRepositories(null, minStars, nextStars));

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return repos;
	}

	private List<PullRequest> extractPRs(JsonNode pullRequests) {
		List<PullRequest> prs = new ArrayList<PullRequest>();
		for (JsonNode prEdge: pullRequests.withArray("edges")) {
			JsonNode prJson = prEdge.get("node");
			String title = prJson.get("title").asText();
			int number = prJson.get("number").asInt();
			String state = prJson.get("state").asText();
			boolean draft = prJson.get("isDraft").asBoolean();
			String baseRepository = prJson.get("baseRepository").get("nameWithOwner").asText();
			String createdAt = prJson.get("createdAt").asText();
			String publishedAt = prJson.get("publishedAt").asText();
			String mergedAt = prJson.get("mergedAt").asText();
			String closedAt = prJson.get("closedAt").asText();

			PullRequest pr = new PullRequest(title, number, baseRepository,
				State.valueOf(state), draft, createdAt, publishedAt, mergedAt, closedAt);
			prs.add(pr);
		}
		return prs;
	}

//	private boolean isActive(String lastActivity, int lastAllowedActivity) {
//		if (!lastActivity.isEmpty()) {
//			LocalDate date = Date.from(Instant.parse(lastActivity)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//			return isActive(date, lastAllowedActivity);
//		}
//		return false;
//	}
//
//	private boolean isActive(LocalDate lastActivity, int lastAllowedActivity) {
//		LocalDate now = LocalDate.now();
//		Duration duration = Duration.between(lastActivity.atStartOfDay(), now.atStartOfDay());
//		return duration.toDays() <= lastAllowedActivity;
//	}

	private boolean isRelevantClient(String owner, String repo, int minStars, int maxStars) {
		String query = Queries.GRAPHQL_CLIENT_QUERY
			.formatted(owner, repo, minStars, maxStars, REPO_LAST_PUSHED_DATE);
		System.out.println(query);

		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
		boolean valid = true;

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");

			// Expect only one edge
			for (var edge: search.withArray("edges")) {
				JsonNode repoJson = edge.get("node");
				boolean isDisabled = repoJson.get("isDisabled").asBoolean();
				boolean isEmpty = repoJson.get("isEmpty").asBoolean();
				boolean isLocked = repoJson.get("isLocked").asBoolean();
				String lastPush = repoJson.get("pushedAt").asText();
				int stars = repoJson.get("stargazerCount").asInt();
				boolean isMaven = repoJson.get("pom").hasNonNull("oid");
				boolean isGradle = repoJson.get("gradle").hasNonNull("oid");

				if (!isMaven || !isGradle)
					return false;

				if (isDisabled || isEmpty || isLocked)
					return false;
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return valid;
	}

	private Map<String, Integer> fetchClientsPerPackage(Repository repo) {
		var res = new HashMap<String, Integer>();
		String url = "https://github.com/%s/%s/network/dependents".formatted(repo.getOwner(), repo.getName());
		// TODO: test purposes
		//String url = "https://github.com/forge/roaster/network/dependents";
		var doc = fetchPage(url);

		if (doc != null) {
			var packageUrls = doc.select("#dependents .select-menu-item").eachAttr("href");

			for (var packageUrl : packageUrls) {
				var pkgPage = fetchPage("https://github.com" + packageUrl);

				if (pkgPage != null) {
					var pkgName = pkgPage.select("#dependents .select-menu-button").text().replace("Package: ", "");
					List<String> clients = pkgPage.select("#dependents .Box-row").eachText();
					var element = pkgPage.select("#dependents .table-list-header-toggle a").first();
					if (element != null) {
						try {
							int total = Integer.parseInt(element.text().substring(0, element.text().indexOf(" ")).replace(",", ""));
							res.put(pkgName, total);

							//TODO: modify retrieved information
							int relevant = 0;
							for (String client : clients) {
								String[] clientArray = client.split(" ");
								String clientOwner = clientArray[0];
								String clientRepo = clientArray[2];

								if (isRelevantClient(clientOwner, clientRepo, 1, REPO_MAX_STARS))
									relevant++;
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		return res;
	}

	private Document fetchPage(String url) {
		var ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
		var ref = "http://www.google.com";

		try {
			Thread.sleep(250);
			return Jsoup.connect(url).userAgent(ua).referrer(ref).get();
		} catch (HttpStatusException e) {
			if (e.getStatusCode() == 429) {
				System.out.println("Too many requests, sleeping...");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException ee) {
					ee.printStackTrace();
					Thread.currentThread().interrupt();
				}
				return fetchPage(url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return null;
	}



	public static void main(String[] args) {
		new FetchGitHubRepositories().run();
	}
}
