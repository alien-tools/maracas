package com.github.maracas.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.experiments.model.Package;
import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Release;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;
import com.github.maracas.experiments.utils.GitHubUtil;
import com.github.maracas.experiments.utils.Queries;
import com.github.maracas.experiments.utils.Util;

/**
 * Class in charge of fetching popular Java repositories from GitHub.
 */
public class GitHubRepositoriesFetcher {
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

	/**
	 * Creates a {@link GitHubRepositoriesFetcher} instance.
	 */
	public GitHubRepositoriesFetcher() {
		this.repositories = new ArrayList<Repository>();
	}

	/**
	 * Returns the list of fetched repositories from GitHub. Use it after calling
	 * the {@link GitHubRepositoriesFetcher#fetchRepositories(int, int)}
	 * method.
	 *
	 * @return List of fetched GitHub {@link Repository} instances
	 */
	public List<Repository> getRepositories() {
		return repositories;
	}

	/**
	 * Sets the list of {@link Repository} instances.
	 *
	 * @param repositories List of GitHub {@link Repository} instances
	 */
	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}

	/**
	 * Fetches the list of popular GitHub repositories based on certain criteria.
	 * As side effect, the {@link #repositories} list is populated.
	 *
	 * @param minStars Minimum number of stars per repository
	 * @param maxStars Maximum number of stars per repository
	 */
	public void fetchRepositories(int minStars, int maxStars) {
		repositories = fetchRepositories(null, minStars, maxStars);
	}

	/**
	 * Recursively fetches the list of popular GitHub repositories based on
	 * certain criteria.
	 *
	 * @param cursor   GraphQL query cursor
	 * @param minStars Minimum number of stars per repository
	 * @param maxStars Maximum number of stars per repository
	 * @return List of {@link Repository} instances
	 */
	private List<Repository> fetchRepositories(String cursor, int minStars, int maxStars) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";

		String query = Queries.GRAPHQL_LIBRARIES_QUERY.formatted(minStars, maxStars, REPO_LAST_PUSHED_DATE, cursorQuery);
		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
		List<Repository> repos = new ArrayList<Repository>();

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
				var maven = repoJson.get("pom").hasNonNull("oid");
				var gradle = repoJson.get("gradle").hasNonNull("oid");
				var stars = repoJson.get("stargazerCount").asInt();

				Repository repo = new Repository(owner, name, stars, lastPush, maven, gradle);
				fetchPullRequests(null, repo);
				fetchPackages(null, repo);

				if (stars < nextStars)
					nextStars = stars;

				if (!lastPR.isEmpty() && (maven || gradle)) {
					var lastMerged = Util.stringToLocalDate(lastPR.get(0));
					if (Util.isActive(lastMerged, PR_LAST_MERGED_IN_DAYS))
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

	/**
	 * Recursively fetches the pull requests of a given repository.
	 *
	 * @param cursor GraphQL query cursor
	 * @param repo   Target repository
	 */
	private void fetchPullRequests(String cursor, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PRS_QUERY.formatted(repo.getOwner(),
			repo.getName(), String.join(", ", new String[] {State.OPEN.toString(), State.MERGED.toString(), State.CLOSED.toString()}),
			cursorQuery);
		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode pullRequests = search.withArray("edges").get(0)
				.get("node").get("pullRequests");
			JsonNode pageInfo = pullRequests.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode prEdge: pullRequests.withArray("edges"))
				extractPRs(prEdge, repo);

			if (hasNextPage)
				fetchPullRequests(endCursor, repo);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extracts a pull requests from a repository pull request edge JSON node.
	 * For more information about the structure, check the
	 * {@link Queries#GRAPHQL_PRS_QUERY}.
	 *
	 * @param prEdge JSON node pointing to a "pullRequests" edge
	 * @param repo   Repository where the PR is being merged
	 */
	private void extractPRs(JsonNode prEdge, Repository repo) {
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

		PullRequest pullRequest = new PullRequest(title, number, repo, baseRepository,
			State.valueOf(state), draft, createdAt, publishedAt, mergedAt, closedAt);
		fetchPRFiles(null, pullRequest);
		// TODO: 1. find PR release; 2. add it to the PR; 3. add it to the repo
		repo.addPullRequest(pullRequest);
	}

	private void fetchPackages(String cursor, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PACKAGES_QUERY.formatted(repo.getOwner(),
			repo.getName(), cursorQuery);
		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode packages = search.withArray("edges").get(0)
				.get("node").get("packages");
			JsonNode pageInfo = packages.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode pkgNode: packages.withArray("nodes"))
				extractPackage(pkgNode, repo);

			if (hasNextPage)
				fetchPackages(endCursor, repo);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void extractPackage(JsonNode pkgNode, Repository repo) {
		String name = pkgNode.get("name").asText();
		Package pkg = new Package(name, repo);
		fetchReleases(null, pkg, repo);
	}

	private void fetchReleases(String cursor, Package pkg, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PACKAGES_QUERY.formatted(repo.getOwner(),
			repo.getName(), pkg.getName(), cursorQuery);
		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode pkgJson = search.withArray("edges").get(0)
				.get("node").get("pkg");
			JsonNode versions = pkgJson.get("versions");
			JsonNode pageInfo = versions.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode versionNode: versions.withArray("nodes"))
				extractRelease(versionNode, pkg, repo);

			if (hasNextPage)
				fetchReleases(endCursor, pkg, repo);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void extractRelease(JsonNode versionNode, Package pkg, Repository repo) {
		String version = versionNode.get("version").asText();
		Release release = null;

		if (!repo.releaseExists(version)) {
			JsonNode file = versionNode.get("f").get("nodes");
			if (file != null) {
				String dateString = file.get(0).get("updatedAt").asText();
				LocalDate date = Util.stringToLocalDate(dateString);
				release = new Release(version, date, repo);
			}
		} else {
			release = repo.getRelease(version);
		}

		if (release != null) {
			pkg.setRelease(release);
			release.addPackage(pkg);
			repo.addPackage(pkg);
		}
	}

	/**
	 * Recursively fetches the relative paths of files modified in a given pull
	 * request.
	 *
	 * @param cursor      GraphQL query cursor
	 * @param pullRequest Target pull request
	 */
	private void fetchPRFiles(String cursor, PullRequest pullRequest) {
		Repository repo = pullRequest.getRepository();
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PRS_FILES_QUERY.formatted(repo.getOwner(), repo.getName(),
			pullRequest.getNumber(), cursorQuery);
		ResponseEntity<String> response = GitHubUtil.postQuery(query, GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode pr = search.withArray("edges").get(0).get("node").get("pr");
			JsonNode files = pr.get("files");
			JsonNode pageInfo = files.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode fileEdge: files.withArray("edges")) {
				String file = fileEdge.get("node").get("path").asText();
				pullRequest.addFile(file);
			}

			if (hasNextPage)
				fetchPRFiles(endCursor, pullRequest);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetches the list of packages and package clients of a given repository.
	 * As a side effect, the {@link Repository} packages list is modified.
	 *
	 * @param repo Target {@link Repository} instance
	 */
	private void fetchRepoPackagesAndClients(Repository repo) {
		String url = "https://github.com/%s/%s/network/dependents".formatted(repo.getOwner(), repo.getName());
		// TODO: test purposes
		//String url = "https://github.com/forge/roaster/network/dependents";
		Document doc = GitHubUtil.fetchPage(url);

		if (doc != null) {
			List<String> packageUrls = doc.select("#dependents .select-menu-item").eachAttr("href");

			for (String packageUrl: packageUrls) {
				Document pkgPage = GitHubUtil.fetchPage("https://github.com" + packageUrl);

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

	/**
	 * Extracts the list of relevant clients given a list of strings representing
	 * the client repositories (e.g. {"google/guava", "alien-tools/maracas"}).
	 *
	 * @param clientRepos List of string representing client repositories
	 *                    (e.g. {"google/guava", "alien-tools/maracas"})
	 * @param minStars    Minimum number of stars per client repository
	 * @param maxStars    Maximum number of stars per client repository
	 * @return List of client {@link Repository} instances
	 */
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

	// TODO: remove after refactoring
	public void run() {
		var repos = fetchRepositories(null, REPO_MIN_STARS, REPO_MAX_STARS);
		System.out.println("Found " + repos.size());

		try (FileWriter csv = new FileWriter("output.csv")) {
			csv.write("owner,name,stars,package,clients,maven,gradle\n");
			csv.flush();

			for (var repo : repos) {
				fetchRepoPackagesAndClients(repo);
				var packages = repo.getRepoPackages();
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

	// TODO: Remove after refactoring
	public static void main(String[] args) {
		new GitHubRepositoriesFetcher().run();
	}
}
