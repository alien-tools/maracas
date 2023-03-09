package com.github.maracas.experiments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.maracas.experiments.csv.CSVManager;
import com.github.maracas.experiments.csv.ClientsCSVManager;
import com.github.maracas.experiments.csv.PullRequestsCSVManager;
import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;
import com.github.maracas.experiments.utils.Constants;
import com.github.maracas.experiments.utils.Constants.ExperimentErrorCode;
import com.github.maracas.experiments.utils.GitHubUtil;
import com.github.maracas.experiments.utils.Queries;
import com.github.maracas.experiments.utils.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Class in charge of fetching popular Java repositories from GitHub.
 */
public class GitHubRepositoriesFetcher {
	/**
	 * Class logger
	 */
	private static final Logger logger = LogManager.getLogger(GitHubRepositoriesFetcher.class);

	/**
	 * URL to GitHub search
	 */
	public static final String GITHUB_SEARCH = "https://api.github.com/search";

	/**
	 * URL to the GitHub GraphQL API
	 */
	public static final String GITHUB_GRAPHQL = "https://api.github.com/graphql";

	/**
	 * URL to the GitHub REST API
	 */
	public static final String GITHUB_REST = "https://api.github.com";

	/**
	 * Pointer to the GitHub access token
	 */
	private static final String GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN");


	/**
	 * List of GitHub repositories considered for the experiment
	 */
	private List<Repository> repositories;

	/**
	 * Clients CSV manager
	 */
	private CSVManager clientsCsv;

	/**
	 * Pull requests CSV manager
	 */
	private CSVManager pullRequestsCsv;

	/**
	 * Number of analyzed repositories
	 */
	private int analyzedCases;

	/**
	 * Number of repositories to be analyzed
	 */
	private int totalCases;

	/**
	 * Initial date of the experiment
	 */
	private LocalDate initialDate;


	/**
	 * Creates a {@link GitHubRepositoriesFetcher} instance.
	 */
	public GitHubRepositoriesFetcher(LocalDate initialDate) {
		this.repositories = new ArrayList<Repository>();
		this.analyzedCases = 0;
		this.totalCases = 0;
		this.initialDate = initialDate;

		try {
			clientsCsv = new ClientsCSVManager(Constants.CLIENTS_CSV_PATH);
			pullRequestsCsv = new PullRequestsCSVManager(Constants.PRS_CSV_PATH);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Writes a new record on the output file.
	 *
	 * @param pkg
	 */
	private void writeCSVClientRecords(RepositoryPackage pkg) {
		clientsCsv.writeRecord(pkg);
	}

	/**
	 * Returns the list of fetched repositories from GitHub.
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
	 * Returns the last cursor written on the clients CSV file.
	 *
	 * @return last cursor written on the clients CSV file; {@code null} if the
	 * file does not exist.
	 */
	public String getLastCursor() {
		return clientsCsv.getCursor();
	}

	/**
	 * Returns the last pushed at date written on the clients CSV file.
	 *
	 * @return last pushed at date written on the clients CSV file.
	 * @throws IOException
	 */
	public LocalDateTime getLastDate() throws IOException {
		return ((ClientsCSVManager) clientsCsv).getCurrentDate();
	}

	/**
	 * Recursively fetches the list of popular GitHub repositories based on
	 * certain criteria.
	 *
	 * @param currentCursor   GraphQL query cursor
	 * @return List of {@link Repository} instances
	 */
	public void fetchRepositories(String currentCursor, LocalDateTime currentDate) {
		String cursorQuery = currentCursor != null
			? ", after: \"" + currentCursor + "\""
				: "";

		//		LocalDateTime previousDate = currentDate.minusHours(12);
		//		String query = Queries.GRAPHQL_LIBRARIES_QUERY.formatted(Constants.REPO_MIN_STARS,
		//			GitHubUtil.toGitHubDateFormat(previousDate),
		//			GitHubUtil.toGitHubDateFormat(currentDate), cursorQuery);
		String query = Queries.GRAPHQL_LIBRARIES_QUERY.formatted(Constants.REPO_MIN_STARS, Constants.REPO_MAX_STARS,
			GitHubUtil.toGitHubDateFormat(Constants.REPO_LAST_PUSHED_DATE), cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);

			if (response != null) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(response.getBody());
				JsonNode data = json.get("data");

				if (data != null) {
					JsonNode search = data.get("search");
					int repositoryCount = search.get("repositoryCount").asInt();
					JsonNode pageInfo = search.get("pageInfo");
					boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
					String endCursor = pageInfo.get("endCursor").asText();
					totalCases += repositoryCount;

					for (JsonNode repoEdge : search.withArray("edges")) {
						analyzedCases++;
						logger.info("Count: {} of {}", analyzedCases, totalCases);

						String cursor = repoEdge.get("cursor").asText();
						JsonNode repoJson = repoEdge.get("node");
						String nameWithOwner = repoJson.get("nameWithOwner").asText();
						String[] fields = nameWithOwner.split("/");
						String owner = fields[0];
						String name = fields[1];
						boolean disabled = repoJson.get("isDisabled").asBoolean();
						boolean empty = repoJson.get("isEmpty").asBoolean();
						boolean locked = repoJson.get("isLocked").asBoolean();
						int stars = repoJson.get("stargazerCount").asInt();
						String createdAt = repoJson.get("createdAt").asText();
						String pushedAt = repoJson.get("pushedAt").asText();
						String sshUrl = repoJson.get("sshUrl").asText();
						String url = repoJson.get("url").asText();
						boolean maven = repoJson.get("pom").hasNonNull("oid");
						boolean gradle = repoJson.get("gradle").hasNonNull("oid");

						JsonNode prs = repoJson.get("pullRequests");
						List<String> lastPR = prs.findValuesAsText("mergedAt");

						if (maven && !disabled && !empty && !locked && !lastPR.isEmpty()
							&& Util.isActive(lastPR.get(0), initialDate, Constants.PR_LAST_MERGED_IN_DAYS)) {
							Repository repo = new Repository(owner, name, stars, createdAt,
								pushedAt, sshUrl, url, maven, gradle, cursor);

//							logger.info("Fetching {}/{} POM files...", owner, name);
//							fetchPomFiles(repo);

							logger.info("Fetching {}/{} clients...", owner, name);
							fetchRepoPackagesAndClients(repo);

							logger.info("Found {} total clients for {}/{}", repo.getClients(), owner, name);

							if (repo.getClients() >= Constants.REPO_MIN_CLIENTS) {
								logger.info("Fetching {}/{} pull requests...", owner, name);
								LocalDateTime datetime = LocalDateTime.now();
								fetchPullRequests(null, datetime, repo);
								repositories.add(repo);
							} else {
								logger.info("{}/{} has too few clients, skipping", owner, name);
							}
						} else {
							logger.warn("{} {}/{} cursor:{} [disabled: {}, empty: {}, locked: {}, "
									+ "maven: {}, active: {}]", ExperimentErrorCode.IRRELEVANT_REPO,
								owner, name, cursor, disabled, empty, locked, maven, lastPR.isEmpty());
						}
					}

					if (hasNextPage)
						fetchRepositories(endCursor, currentDate);
					//				else if (previousDate.isAfter(Constants.REPO_LAST_PUSHED_DATE))
					//					fetchRepositories(null, previousDate);

				} else {
					for (JsonNode error : json.withArray("errors")) {
						String type = error.get("type").asText();
						logger.error("{}:{} cursor:{}", ExperimentErrorCode.EXCEEDED_RATE_LIMIT,
							type, currentCursor);
					}

					if (!response.getStatusCode().equals(HttpStatus.OK))
						logger.error("{} cursor:{}: {}/{}", ExperimentErrorCode.GITHUB_API_ERROR,
							response.getStatusCode(), currentCursor, response.getBody());

					try {
						logger.info("Too many requests, sleeping...");
						Thread.sleep(30000);
						fetchRepositories(currentCursor, currentDate);
					} catch (InterruptedException e) {
						logger.error(e);
						Thread.currentThread().interrupt();
					}
				}
			}
		} catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
			logger.error(e);
		}
	}

	/**
	 * Recursively fetches the pull requests of a given repository.
	 *
	 * @param currentCursor GraphQL query cursor
	 * @param repo   Target repository
	 */
	private void fetchPullRequests(String currentCursor, LocalDateTime currentDate, Repository repo) {
		String cursorQuery = currentCursor != null
			? ", after: \"" + currentCursor + "\""
				: "";
//		LocalDateTime previousDate = currentDate.minusHours(12);
//		String query = Queries.GRAPHQL_PRS_QUERY.formatted(repo.getOwner(),
//			repo.getName(), GitHubUtil.toGitHubDateFormat(previousDate),
//			GitHubUtil.toGitHubDateFormat(currentDate), cursorQuery);
		String query = Queries.GRAPHQL_PRS_QUERY.formatted(repo.getOwner(),
			repo.getName(), GitHubUtil.toGitHubDateFormat(Constants.PR_LAST_CREATED)
			, cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
			if (response != null) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(response.getBody());
				JsonNode data = json.get("data");

				if (data != null) {
					JsonNode search = data.get("search");
					JsonNode pageInfo = search.get("pageInfo");
					boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
					String endCursor = pageInfo.get("endCursor").asText();

					for (JsonNode prEdge: search.withArray("edges"))
						extractPRs(prEdge, repo);

					if (hasNextPage)
						fetchPullRequests(endCursor, currentDate, repo);
//					else if (previousDate.isAfter(Constants.PR_LAST_CREATED))
//						fetchPullRequests(null, previousDate, repo);

				} else {
					logger.error(json);
					try {
						logger.info("Too many requests, sleeping...");
						Thread.sleep(30000);
						fetchPullRequests(currentCursor, currentDate, repo);
					} catch (InterruptedException e) {
						logger.error(e);
						Thread.currentThread().interrupt();
					}
				}
			}

		} catch (JsonProcessingException e) {
			logger.error(e);
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
		try {
			JsonNode prJson = prEdge.get("node");
			String title = prJson.get("title").asText();
			int number = prJson.get("number").asInt();
			String state = prJson.get("state").asText();
			boolean draft = prJson.get("isDraft").asBoolean();
			String baseRepository = prJson.get("baseRepository").get("nameWithOwner").asText();
			String baseRef = prJson.get("baseRef").get("name").asText();
			String baseRefPrefix = prJson.get("baseRef").get("prefix").asText();
			String headRepository = prJson.get("headRepository").get("nameWithOwner").asText();
			String headRef = prJson.get("headRef").get("name").asText();
			String headRefPrefix = prJson.get("headRef").get("prefix").asText();
			String createdAt = prJson.get("createdAt").asText();
			String publishedAt = prJson.get("publishedAt").asText();
			String mergedAt = prJson.get("mergedAt").asText();
			String closedAt = prJson.get("closedAt").asText();
			JsonNode files = prJson.get("files");

			PullRequest pullRequest = new PullRequest(title, number, repo, baseRepository,
				baseRef, baseRefPrefix, headRepository, headRef, headRefPrefix,
				State.valueOf(state), draft, createdAt, publishedAt, mergedAt, closedAt);

			for (JsonNode fileEdge: files.withArray("edges")) {
				String file = fileEdge.get("node").get("path").asText();
				pullRequest.addFile(file);
			}

			logger.info("{}/{} Pull request: {} ({}) - {}", repo.getOwner(), repo.getName(),
				title, number, state);

			pullRequestsCsv.writeRecord(pullRequest);
			repo.addPullRequest(pullRequest);
		} catch (NullPointerException e) {
			logger.error(e);
		}
	}

	/**
	 * Fetches the list of packages and package clients of a given repository.
	 * As a side effect, the {@link Repository} packages list is modified.
	 *
	 * @param repo Target {@link Repository} instance
	 */
	private void fetchRepoPackagesAndClients(Repository repo) {
		String url = "https://github.com/%s/%s/network/dependents"
			.formatted(repo.getOwner(), repo.getName());
		Document doc = GitHubUtil.fetchPage(url);

		if (doc != null) {
			List<String> packageRelUrls = doc.select("#dependents .select-menu-item").eachAttr("href");
			repo.setGitHubPackages(packageRelUrls.size());
			int clients = 0;

			for (String packageRelUrl: packageRelUrls) {
				String packageUrl = "https://github.com" + packageRelUrl;
				Document pkgPage = GitHubUtil.fetchPage(packageUrl);

				if (pkgPage != null) {
					String name = pkgPage.select("#dependents .select-menu-button").text().replace("Package: ", "");
					List<String> clientRepos = new ArrayList<>();
					Element element = pkgPage.select("#dependents .table-list-header-toggle a").first();

					if (element != null) {
						try {
							String clientsStr = element.text().substring(0, element.text().indexOf(" ")).replace(",", "");
							clients += Integer.parseInt(clientsStr);
						} catch (NumberFormatException e) {
							logger.error(e);
						}
					} else {
						logger.warn("{} {}/{} cursor:{} [packageUrl: {}]",
							ExperimentErrorCode.NO_PKG_DEPENDANTS, repo.getOwner(),
							repo.getName(), repo.getCursor(), packageUrl);
					}
				} else {
					logger.warn("{} {}/{} cursor:{} [url: {}]",
						ExperimentErrorCode.NO_PKG_DEPENDANTS, repo.getOwner(),
						repo.getName(), repo.getCursor(), url);
				}
			}

			repo.setClients(clients);
		} else {
			logger.warn("{} {}/{} cursor:{} [url: {}]", ExperimentErrorCode.NO_DEPENDANTS_PAGE,
				repo.getOwner(), repo.getName(), repo.getCursor(), url);
		}
	}

	/**
	 * Fetches all client repositories as displayed in the GitHub dependency
	 * network web page.
	 *
	 * @param url          URL pointing to the dependent repositories
	 * @param clientRepos  List of client repositories
	 */
	private void fetchGitHubClients(String url, List<String> clientRepos) {
		Document pkgPage = GitHubUtil.fetchPage(url);
		if (pkgPage != null) {
			List<String> clients = pkgPage.select("#dependents .Box-row").eachText();
			clientRepos.addAll(clients);
			Elements pagination = pkgPage.select("#dependents .paginate-container .BtnGroup-item");

			if (pagination != null && !pagination.isEmpty()) {
				Element nextBtn = pagination.get(1);
				String nextUrl = nextBtn.attr("abs:href");

				if (!nextUrl.isEmpty())
					fetchGitHubClients(nextUrl, clientRepos);
			}
		}
	}

	/**
	 * Extracts the list of relevant clients given a list of strings representing
	 * the client repositories (e.g. {"google/guava", "alien-tools/maracas"}).
	 *
	 * @param clientRepos List of string representing client repositories
	 *                    (e.g. {"google/guava", "alien-tools/maracas"})
	 * @return List of client {@link Repository} instances
	 */
	private List<Repository> fetchRelevantClients(List<String> clientRepos) {
		List<Repository> relevantClients = new ArrayList<Repository>();
		for (String clientRepo : clientRepos) {
			String[] clientArray = clientRepo.split(" ");
			String owner = clientArray[0];
			String repo = clientArray[2];
			String query = Queries.GRAPHQL_CLIENT_QUERY.formatted(owner, repo);

			try {
				ResponseEntity<String> response = GitHubUtil.postQuery(query,
					GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
				if (response != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode json = mapper.readTree(response.getBody());
					JsonNode data = json.get("data");

					if (response.getStatusCode().equals(HttpStatus.OK)
						&& data != null) {
						JsonNode repository = data.get("repository");

						if (repository != null && !repository.isEmpty()) {
							boolean isArchived = repository.get("isArchived").asBoolean();
							boolean isDisabled = repository.get("isDisabled").asBoolean();
							boolean isEmpty = repository.get("isEmpty").asBoolean();
							boolean isFork = repository.get("isFork").asBoolean();
							boolean isLocked = repository.get("isLocked").asBoolean();
							boolean isMirror = repository.get("isMirror").asBoolean();
							String createdAt = repository.get("createdAt").asText();
							String pushedAt = repository.get("pushedAt").asText();
							int stars = repository.get("stargazerCount").asInt();
							String sshUrl = repository.get("sshUrl").asText();
							String url = repository.get("url").asText();
							ArrayNode languages = repository.get("languages").withArray("edges");
							List<Object> java = new ArrayList<Object>();

							if (!languages.isEmpty())
								java = Arrays.stream(Arrays.asList(languages.get(0)).toArray())
								.filter(x -> ((JsonNode) x).get("node")
									.get("name").asText()
									.equalsIgnoreCase("Java"))
								.collect(Collectors.toList());

							boolean maven = repository.get("pom").hasNonNull("oid");
							boolean gradle = repository.get("gradle").hasNonNull("oid");

							if (maven && !isArchived && !isDisabled && !isEmpty && !isFork
								&& !isMirror && !isLocked && !java.isEmpty()
								&& stars >= Constants.CLIENT_MIN_STARS) {
								Repository client = new Repository(owner, repo, stars,
									createdAt, pushedAt, sshUrl, url, maven, gradle, null);
								relevantClients.add(client);

								logger.info("   Client: {}/{}", owner, repo);
							}
						}
					}
				}

			} catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
				logger.error(e);
			}
		}
		return relevantClients;
	}
}
