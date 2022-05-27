package com.github.maracas.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;
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
			csv.write("owner,name,stars,package,clients,maven,gradle\n");
			csv.flush();

			for (var repo : repos) {
				fetchClientsPerPackage(repo);
				var packages = repo.getPackages();
				var total = packages.size();
				System.out.printf("%s has %d clients%n", repo, total);
				csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.getOwner(),
					repo.getName(), repo.getStars(), "total", total,
					repo.isMaven(), repo.isGradle()));

				for (var pkg : packages) {
					System.out.printf("\t%s: %s", pkg.name(), pkg.relevantClients().size());
					csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.getOwner(),
						repo.getName(), repo.getStars(), pkg.name(), pkg.relevantClients().size(),
						repo.isMaven(), repo.isGradle()));
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
				var isMaven = repoJson.get("pom").hasNonNull("oid");
				var isGradle = repoJson.get("gradle").hasNonNull("oid");
				var stars = repoJson.get("stargazerCount").asInt();

				Repository repo = new Repository(owner, name, stars, lastPush, isMaven, isGradle);
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

	private void fetchClientsPerPackage(Repository repo) {
		String url = "https://github.com/%s/%s/network/dependents".formatted(repo.getOwner(), repo.getName());
		// TODO: test purposes
		//String url = "https://github.com/forge/roaster/network/dependents";
		Document doc = Util.fetchPage(url);

		if (doc != null) {
			List<String> packageUrls = doc.select("#dependents .select-menu-item").eachAttr("href");

			for (String packageUrl: packageUrls) {
				Document pkgPage = Util.fetchPage("https://github.com" + packageUrl);

				if (pkgPage != null) {
					String name = pkgPage.select("#dependents .select-menu-button").text().replace("Package: ", "");
					List<String> clientRepos = pkgPage.select("#dependents .Box-row").eachText();
					Element element = pkgPage.select("#dependents .table-list-header-toggle a").first();

					if (element != null) {
						try {
							int clients = Integer.parseInt(element.text().substring(0, element.text().indexOf(" ")).replace(",", ""));
							List<Repository> relevantClients = extractRelevantClients(clientRepos, REPO_MIN_STARS, REPO_MAX_STARS);
							RepositoryPackage pkg = new RepositoryPackage(name, repo, clients, relevantClients);
							repo.addPackage(pkg);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private List<Repository> extractRelevantClients(List<String> clientRepos, int minStars, int maxStars) {
		List<Repository> relevantClients = new ArrayList<Repository>();
		for (String clientRepo : clientRepos) {
			String[] clientArray = clientRepo.split(" ");
			String owner = clientArray[0];
			String repo = clientArray[2];

			String query = Queries.GRAPHQL_CLIENT_QUERY
				.formatted(owner, repo, minStars, maxStars, REPO_LAST_PUSHED_DATE);
			ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
			boolean valid = true;

			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(response.getBody());
				JsonNode search = json.get("data").get("search");

				// Expect only one edge
				for (JsonNode edge: search.withArray("edges")) {
					JsonNode repoJson = edge.get("node");
					boolean isDisabled = repoJson.get("isDisabled").asBoolean();
					boolean isEmpty = repoJson.get("isEmpty").asBoolean();
					boolean isLocked = repoJson.get("isLocked").asBoolean();
					String lastPush = repoJson.get("pushedAt").asText();
					int stars = repoJson.get("stargazerCount").asInt();
					boolean maven = repoJson.get("pom").hasNonNull("oid");
					boolean gradle = repoJson.get("gradle").hasNonNull("oid");

					if ((maven || gradle) && (isDisabled || isEmpty || isLocked)) {
						Repository client = new Repository(owner, repo, stars, lastPush, maven, gradle);
						relevantClients.add(client);
					}
				}

			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return relevantClients;
	}

	public static void main(String[] args) {
		new FetchGitHubRepositories().run();
	}
}
