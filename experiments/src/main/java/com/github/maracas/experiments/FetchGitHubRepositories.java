package com.github.maracas.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchGitHubRepositories {
	public static final int REPO_MIN_STARS = 10;
	public static final int REPO_MAX_STARS = 100;
	public static final String REPO_LAST_PUSHED_DATE = "2022-02-12";
	public static final int PR_LAST_MERGED_IN_DAYS = 90;
	public static final int LAST_PUSH_IN_DAYS = 90;
	public static final String GITHUB_GRAPHQL = "https://api.github.com/graphql";

	record Repository(String owner, String name, int stars, int mergedPRs, boolean maven, boolean gradle) {}

	public void run() {
		var repos = readPage(null, REPO_MIN_STARS, 9999999);
		System.out.println("Found " + repos.size());

		try (FileWriter csv = new FileWriter("output.csv")) {
			csv.write("owner,name,stars,package,clients,mergedPrs,maven,gradle\n");
			csv.flush();

			for (var repo : repos) {
				var packages = fetchClientsPerPackage(repo);
				var total = packages.values().stream().reduce(0, Integer::sum);
				System.out.printf("%s has %d clients%n", repo, total);
				csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.owner(), repo.name(), repo.stars(), "total", total,
					repo.mergedPRs(), repo.maven(), repo.gradle()));

				for (var pkg : packages.entrySet()) {
					System.out.printf("\t%s: %s", pkg.getKey(), pkg.getValue());
					csv.write("%s,%s,%d,%s,%d,%d,%b,%b%n".formatted(repo.owner(), repo.name(), repo.stars(), pkg.getKey(), pkg.getValue(),
						repo.mergedPRs(), repo.maven(), repo.gradle()));
				}

				csv.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Repository> readPage(String cursor, int minStars, int maxStars) {
		var cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";

		var graphqlQuery = """
			query {
			  search(
			    type: REPOSITORY,
			    query: "language:java stars:%d..%d pushed:>%s mirror:false archived:false sort:stars-desc",
			    first: 100
			    %s
			  ) {
			    repositoryCount

			    edges {
			      node {
			        ... on Repository {
			          nameWithOwner
			          stargazerCount

			          pom: object(expression: "HEAD:pom.xml") {
			            oid
			          }

			          gradle: object(expression: "HEAD:build.gradle") {
			            oid
			          }

			          pullRequests(states: [MERGED], last: 1) {
			            mergedPRs: totalCount

			            edges {
			              node {
			                mergedAt
			              }
			            }
			          }
			        }
			      }
			    }

			    pageInfo {
			      hasNextPage
			      endCursor
			    }
			  }
			}""".formatted(minStars, maxStars, REPO_LAST_PUSHED_DATE, cursorQuery);

		System.out.println(graphqlQuery);

		var rest = new RestTemplate();
		var headers = new HttpHeaders();
		headers.add("Authorization", "Bearer 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");
		headers.add("Content-Type", "application/graphql");
		var jsonQuery = graphqlAsJson(graphqlQuery);
		var response = rest.postForEntity(GITHUB_GRAPHQL, new HttpEntity<>(jsonQuery, headers), String.class);
		System.out.println(response);

		var repos = new ArrayList<Repository>();
		try {
			var mapper = new ObjectMapper();
			var json = mapper.readTree(response.getBody());
			var search = json.get("data").get("search");
			var pageInfo = search.get("pageInfo");
			var hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			var endCursor = pageInfo.get("endCursor").asText();

			var nextStars = maxStars;
			for (var edge: search.withArray("edges")) {
				var repoJson = edge.get("node");
				var nameWithOwner = repoJson.get("nameWithOwner").asText();
				var fields = nameWithOwner.split("/");
				var owner = fields[0];
				var name = fields[1];
				var prs = repoJson.get("pullRequests");
				var lastPR = prs.findValuesAsText("mergedAt");
				var mergedPRs = prs.get("mergedPRs").asInt();
				var isMaven = repoJson.get("pom").hasNonNull("oid");
				var isGradle = repoJson.get("gradle").hasNonNull("oid");
				var stars = repoJson.get("stargazerCount").asInt();
				var repo = new Repository(owner, name, stars, mergedPRs, isMaven, isGradle);

				if (stars < nextStars)
					nextStars = stars;

				if (!lastPR.isEmpty() && (isMaven || isGradle)) {
					var lastMerged = Date.from(Instant.parse(lastPR.get(0))).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					var now = LocalDate.now();
					var between = Duration.between(lastMerged.atStartOfDay(), now.atStartOfDay());

					if (between.toDays() <= PR_LAST_MERGED_IN_DAYS)
						repos.add(repo);
					else
						System.out.printf("%s last PR merged on %s, skipping.%n", repo, lastMerged);
				}
			}

			if (hasNextPage)
				repos.addAll(readPage(endCursor, minStars, maxStars));
			else if (nextStars > minStars) {
				repos.addAll(readPage(null, minStars, nextStars));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return repos;
	}

	private ResponseEntity<String> postQuery(String query) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer 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");
		headers.add("Content-Type", "application/graphql");
		String jsonQuery = graphqlAsJson(query);
		ResponseEntity<String> response = rest.postForEntity(GITHUB_GRAPHQL,
			new HttpEntity<>(jsonQuery, headers), String.class);
		return response;
	}

	private boolean isActive(String lastActivity, int lastAllowedActivity) {
		if (!lastActivity.isEmpty()) {
			LocalDate date = Date.from(Instant.parse(lastActivity)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return isActive(date, lastAllowedActivity);
		}
		return false;
	}

	private boolean isActive(LocalDate lastActivity, int lastAllowedActivity) {
		LocalDate now = LocalDate.now();
		Duration duration = Duration.between(lastActivity.atStartOfDay(), now.atStartOfDay());
		return duration.toDays() <= lastAllowedActivity;
	}

	private boolean isRelevantClient(String owner, String repo, int minStars, int maxStars) {
		String query = """
			query {
			  repository(owner:%s, name:%s) {
			    isArchived
			    isDisabled
			  	isEmpty
			    isFork
			    isLocked
			  	languages(first:100) {
			      nodes {
			        name
			      }
			  	}
			    pushedAt
			    stargazerCount
			    pom: object(expression: "HEAD:pom.xml") {
			      oid
			    }
			    gradle: object(expression: "HEAD:build.gradle") {
			      oid
			    }
			  }
			}
			""".formatted(owner, repo);

		ResponseEntity<String> response = postQuery(query);
		boolean valid = true;

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode repoJson = search.get("repository");

			boolean isArchived = repoJson.get("isArchived").asBoolean();
			boolean isDisabled = repoJson.get("isDisabled").asBoolean();
			boolean isEmpty = repoJson.get("isEmpty").asBoolean();
			boolean isFork = repoJson.get("isFork").asBoolean();
			boolean isLocked = repoJson.get("isLocked").asBoolean();
			String lastPush = repoJson.get("pushedAt").asText();
			int stars = repoJson.get("stargazerCount").asInt();
			boolean isMaven = repoJson.get("pom").hasNonNull("oid");
			boolean isGradle = repoJson.get("gradle").hasNonNull("oid");
			JsonNode languagesJson = repoJson.get("languages");
			List<String> languages = languagesJson.findValuesAsText("name");

			if (!isMaven || !isGradle)
				return false;

			if (isArchived || isDisabled || isEmpty || isFork || isLocked)
				return false;

			if (stars < minStars || stars > maxStars)
				return false;

			if (!isActive(lastPush, LAST_PUSH_IN_DAYS))
				return false;

			if (!languages.contains("Java"))
				return false;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return valid;
	}

	private Map<String, Integer> fetchClientsPerPackage(Repository repo) {
		var res = new HashMap<String, Integer>();
		String url = "https://github.com/%s/%s/network/dependents".formatted(repo.owner(), repo.name());
		var doc = fetchPage(url);

		if (doc != null) {
			var packageUrls = doc.select("#dependents .select-menu-item").eachAttr("href");

			for (var packageUrl : packageUrls) {
				var pkgPage = fetchPage("https://github.com" + packageUrl);

				if (pkgPage != null) {
					var pkgName = pkgPage.select("#dependents .select-menu-button").text().replace("Package: ", "");
					var clients = pkgPage.select("#dependents #dg-repo-pkg-dependent");

					//var element = pkgPage.select("#dependents .table-list-header-toggle a").first();
					//if (element != null) {
					//	try {
					//		var count = Integer.parseInt(element.text().substring(0, element.text().indexOf(" ")).replace(",", ""));
					//		res.put(pkgName, count);
					//	} catch (NumberFormatException e) {
					//		e.printStackTrace();
					//	}
					//}

					// Only report relevant clients
					var count = 0;
					for (var client : clients) {
						var clientOwner = client.select("#dependents a").first().text();
						var clientRepo = client.select("#dependents a").last().text();
						if (isRelevantClient(clientOwner, clientRepo, REPO_MIN_STARS, REPO_MAX_STARS))
							count++;
					}

					res.put(pkgName, count);
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

	private String graphqlAsJson(String query) {
		return "{ \"query\": \"" + query.replace("\n", "").replace("\"", "\\\"") + "\"";
	}

	public static void main(String[] args) {
		new FetchGitHubRepositories().run();
	}
}
