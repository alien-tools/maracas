package com.github.maracas.experiments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.experiments.csv.CSVManager;
import com.github.maracas.experiments.csv.ClientsCSVManager;
import com.github.maracas.experiments.csv.ErrorRecord;
import com.github.maracas.experiments.csv.ErrorRecord.ExperimentErrorCode;
import com.github.maracas.experiments.csv.ErrorsCSVManager;
import com.github.maracas.experiments.model.Package;
import com.github.maracas.experiments.model.Package.PackageSourceType;
import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Release;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;
import com.github.maracas.experiments.utils.Constants;
import com.github.maracas.experiments.utils.GitHubUtil;
import com.github.maracas.experiments.utils.Queries;
import com.github.maracas.experiments.utils.Util;

/**
 * Class in charge of fetching popular Java repositories from GitHub.
 */
public class GitHubRepositoriesFetcher {
	public static final int REPO_MIN_STARS = 100;
	public static final int CLIENT_MIN_STARS = 2;
	public static final LocalDateTime REPO_LAST_PUSHED_DATE = LocalDateTime.of(2022, 01, 01, 0, 0);
	public static final LocalDateTime CLIENT_LAST_PUSHED_DATE = LocalDateTime.of(2022, 01, 01, 0, 0);
	public static final int PR_LAST_MERGED_IN_DAYS = 180;
	public static final int LAST_PUSH_IN_DAYS = 180;
	public static final String GITHUB_SEARCH = "https://api.github.com/search";
	public static final String GITHUB_GRAPHQL = "https://api.github.com/graphql";
	private static final String GITHUB_ACCESS_TOKEN = System.getenv("GITHUB_ACCESS_TOKEN");

	/**
	 * List of GitHub repositories considered for the experiment
	 */
	private List<Repository> repositories;

	private CSVManager clientsCsv;

	private CSVManager errorsCsv;

	private LocalDateTime currentDate;

	private int analyzedCases;

	private int totalCases;

	/**
	 * Creates a {@link GitHubRepositoriesFetcher} instance.
	 */
	public GitHubRepositoriesFetcher() {
		this.repositories = new ArrayList<Repository>();
		this.analyzedCases = 0;
		this.totalCases = 0;
		this.currentDate = LocalDateTime.now();

		try {
			clientsCsv = new ClientsCSVManager(Constants.CLIENTS_CSV_PATH);
			errorsCsv = new ErrorsCSVManager(Constants.ERRORS_CSV_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCSVErrorRecord(String cursor, String owner, String name,
		ExperimentErrorCode code,  String comments) {
		ErrorRecord error = new ErrorRecord(cursor, owner, name, code, comments);
		error.printLog();
		errorsCsv.writeRecord(error);
	}

	private void writeCSVClientRecords(RepositoryPackage pkg) {
		clientsCsv.writeRecord(pkg);
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
	 * Recursively fetches the list of popular GitHub repositories based on
	 * certain criteria.
	 *
	 * @param currentCursor   GraphQL query cursor
	 * @return List of {@link Repository} instances
	 */
	public void fetchRepositories(String currentCursor) {
		String cursorQuery = currentCursor != null
			? ", after: \"" + currentCursor + "\""
			: "";
		LocalDateTime previousDate = currentDate.minusHours(12);
		String query = Queries.GRAPHQL_LIBRARIES_QUERY.formatted(REPO_MIN_STARS,
			GitHubUtil.toGitHubDateFormat(previousDate),
			GitHubUtil.toGitHubDateFormat(currentDate), cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
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

				for (var repoEdge: search.withArray("edges")) {
					analyzedCases++;
					System.out.println("Count: %d of %d".formatted(analyzedCases, totalCases));

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
					String lastPush = repoJson.get("pushedAt").asText();
					String sshUrl = repoJson.get("sshUrl").asText();
					String url = repoJson.get("url").asText();
					boolean maven = repoJson.get("pom").hasNonNull("oid");
					boolean gradle = repoJson.get("gradle").hasNonNull("oid");

					JsonNode prs = repoJson.get("pullRequests");
					List<String> lastPR = prs.findValuesAsText("mergedAt");

					if (maven && !disabled && !empty && !locked && !lastPR.isEmpty()) {
						LocalDate lastMerged = Util.stringToLocalDate(lastPR.get(0));

						if (Util.isActive(lastMerged, PR_LAST_MERGED_IN_DAYS)) {
							Repository repo = new Repository(owner, name, stars,
								lastPush, sshUrl, url, maven, gradle, cursor);

							//System.out.println("Fetching %s/%s pull requests...".formatted(owner, name));
							//fetchPullRequests(null, repo);

							System.out.println("Fetching %s/%s POM files...".formatted(owner, name));
							fetchPomFiles(repo);

							System.out.println("Fetching %s/%s clients...".formatted(owner, name));
							fetchRepoPackagesAndClients(repo);
							repositories.add(repo);

						} else {
							writeCSVErrorRecord(cursor, owner, name, ExperimentErrorCode.INACTIVE_REPO,
								"{lastMerged: %s}".formatted(lastMerged.toString()));
						}
					} else {
						writeCSVErrorRecord(cursor, owner, name, ExperimentErrorCode.IRRELEVANT_REPO,
							"{disabled: %b, empty: %b, locked: %b, maven: %b, lastPR: %b}"
							.formatted(disabled, empty, locked, maven, lastPR.isEmpty()));
					}

					// FIXME: Using packages won't work here. Only repositories making use of
					// GitHub packages will be part of the dataset.
					//fetchPackages(null, repo);
				}

				if (hasNextPage) {
					fetchRepositories(endCursor);
				} else if (previousDate.isBefore(REPO_LAST_PUSHED_DATE)) {
					currentDate = previousDate;
					fetchRepositories(null);
				}
			} else {
				for (JsonNode error: json.withArray("errors")) {
					String type = error.get("type").asText();
					writeCSVErrorRecord(currentCursor, null, null,
						ExperimentErrorCode.EXCEEDED_RATE_LIMIT, type);
				}

				if (!response.getStatusCode().equals(HttpStatus.OK))
					writeCSVErrorRecord(currentCursor, null, null,
						ExperimentErrorCode.GITHUB_API_ERROR, response.getBody());

				try {
					Thread.sleep(30000);
					fetchRepositories(currentCursor);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}

		} catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursively fetches the pull requests of a given repository.
	 *
	 * @param cursor GraphQL query cursor
	 * @param repo   Target repository
	 */
	public void fetchPullRequests(String cursor, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PRS_QUERY.formatted(repo.getOwner(),
			repo.getName(), ">=2022-05-01", cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode pageInfo = search.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode prEdge: search.withArray("edges"))
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
		System.out.println("Fetching %s/%s pull request #%d (%s) files..."
			.formatted(repo.getOwner(), repo.getName(), number, state));
		fetchPRFiles(null, pullRequest);
		// TODO: 1. find PR release; 2. add it to the PR; 3. add it to the repo
		repo.addPullRequest(pullRequest);
	}

	private void fetchPomFiles(Repository repo) {
		String url = (GITHUB_SEARCH + "/code?q=repo:%s/%s+filename:pom+extension:xml")
			.formatted(repo.getOwner(), repo.getName());
		ResponseEntity<String> response = GitHubUtil.getQuery(url, GITHUB_ACCESS_TOKEN);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());

			for (JsonNode pomNode: json.withArray("items")) {
				String path = pomNode.get("path").asText();
				String pomUrl = pomNode.get("url").asText();

				ResponseEntity<String> pomResponse = GitHubUtil.getQuery(pomUrl, GITHUB_ACCESS_TOKEN);
				JsonNode pomJson = mapper.readTree(pomResponse.getBody());
				String downloadUrl = pomJson.get("download_url").asText();

				if (downloadUrl != null && !path.isEmpty() && path.contains("pom.xml")) {
					MavenXpp3Reader pomReader = new MavenXpp3Reader();

					try {
						URL urll = new URL(downloadUrl);
						Document doc = Jsoup.parse(urll.openStream(), "UTF-8", "", Parser.xmlParser());

						Model model = pomReader.read(new ByteArrayInputStream(doc.toString().getBytes()));
						String groupId = model.getGroupId();
						String artifactId = model.getArtifactId();
						String version = model.getVersion();
						String relativePath = path.substring(0, path.indexOf("pom.xml"));

						if (groupId == null || version == null) {
							Parent parent = model.getParent();

							if (parent != null) {
								groupId = (groupId == null) ? parent.getGroupId() : groupId;
								version = (version == null) ? parent.getVersion() : version;
							}
						}

						if (groupId == null || artifactId == null) {
							writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
								ExperimentErrorCode.INCOMPLETE_POM, "{groupId: %s, artifactId: %s}"
								.formatted(groupId, artifactId));
							continue;
						}

						RepositoryPackage pkg = new RepositoryPackage(groupId,
							artifactId, version, relativePath, repo);
						repo.addPackage(pkg);

						System.out.println("   %s:%s:%s - %s".formatted(groupId,
							artifactId, version, path));
					} catch (XmlPullParserException | IOException e) {
						writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
							ExperimentErrorCode.POM_DOWNLOAD_ISSUE,
							"{downluadUrl: %s, path: %s}".formatted(downloadUrl, path));
						e.printStackTrace();
					}
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	private void fetchPackages(String cursor, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PACKAGES_QUERY.formatted(repo.getOwner(),
			repo.getName(), cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
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

	@Deprecated
	private void extractPackage(JsonNode pkgNode, Repository repo) {
		String name = pkgNode.get("name").asText();
		Package pkg = new Package(name, repo);
		fetchReleases(null, pkg, repo);

		if (pkg.getRelease() != null)
			fetchPkgSource(null, pkg, repo);
	}

	@Deprecated
	private void fetchReleases(String cursor, Package pkg, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_RELEASES_QUERY.formatted(repo.getOwner(),
			repo.getName(), pkg.getName(), cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
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

	@Deprecated
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
		}
	}

	@Deprecated
	private void fetchPkgSource(String cursor, Package pkg, Repository repo) {
		String cursorQuery = cursor != null
			? ", after: \"" + cursor + "\""
			: "";
		String query = Queries.GRAPHQL_PACKAGE_SRC_QUERY.formatted(repo.getOwner(),
			repo.getName(), pkg.getName(), pkg.getRelease().getVersion(), cursorQuery);

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(response.getBody());
			JsonNode search = json.get("data").get("search");
			JsonNode packages = search.withArray("edges").get(0)
				.get("node").get("packages");
			JsonNode version = packages.withArray("nodes").get(0);
			JsonNode files = version.get("files");
			JsonNode pageInfo = version.get("pageInfo");
			boolean hasNextPage = pageInfo.get("hasNextPage").asBoolean();
			String endCursor = pageInfo.get("endCursor").asText();

			for (JsonNode fileNode: files.withArray("nodes"))
				extractPackageSource(fileNode, pkg);

			if (pkg.getSrcUrl().equals(PackageSourceType.UNDEFINED.toString())
				&& hasNextPage)
				fetchPkgSource(endCursor, pkg, repo);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	private void extractPackageSource(JsonNode fileNode, Package pkg) {
		String name = fileNode.get("name").asText();

		if (name.endsWith("-sources.jar")) {
			String url = fileNode.get("url").asText();
			pkg.setSrcUrl(url);
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

		try {
			ResponseEntity<String> response = GitHubUtil.postQuery(query,
				GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
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
				System.out.println("   %s".formatted(file));
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
							RepositoryPackage pkg = repo.getRepoPackage(name);

							if (pkg != null) {
								String clientsStr = element.text().substring(0, element.text().indexOf(" ")).replace(",", "");
								int clients = Integer.parseInt(clientsStr);
								List<Repository> relevantClients = extractRelevantClients(clientRepos);
								pkg.setClients(clients);
								pkg.setRelevantClients(relevantClients);
								writeCSVClientRecords(pkg);
							} else {
								writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
									ExperimentErrorCode.NO_PKG_IN_REPO, "{name: %s}".formatted(name));
							}
						} catch (NumberFormatException e) {
							writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
								ExperimentErrorCode.CLIENTS_NUM_FORMAT_ERROR, "{element: %s}"
								.formatted(element.toString()));
							e.printStackTrace();
						}
					} else {
						writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
							ExperimentErrorCode.NO_PKG_DEPENDANTS, "{packageUrl: %s}"
							.formatted(packageUrl));
					}
				} else {
					writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
						ExperimentErrorCode.EMPTY_PKG_PAGE, "{url: %s}".formatted(url));
				}
			}
		} else {
			writeCSVErrorRecord(repo.getCursor(), repo.getOwner(), repo.getName(),
				ExperimentErrorCode.NO_DEPENDANTS_PAGE, "{url: %s}".formatted(url));
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
	private List<Repository> extractRelevantClients(List<String> clientRepos) {
		List<Repository> relevantClients = new ArrayList<Repository>();
		for (String clientRepo : clientRepos) {
			String[] clientArray = clientRepo.split(" ");
			String owner = clientArray[0];
			String repo = clientArray[2];

			String query = Queries.GRAPHQL_CLIENT_QUERY
				.formatted(owner, repo, CLIENT_MIN_STARS, CLIENT_LAST_PUSHED_DATE);

			try {
				ResponseEntity<String> response = GitHubUtil.postQuery(query,
					GITHUB_GRAPHQL, GITHUB_ACCESS_TOKEN);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode json = mapper.readTree(response.getBody());
				JsonNode data = json.get("data");

				if (response.getStatusCode().equals(HttpStatus.OK) && data != null) {
					JsonNode search = data.get("search");

					// Expect only one edge
					for (JsonNode edge: search.withArray("edges")) {
						var cursor = edge.get("cursor").asText();
						JsonNode repoJson = edge.get("node");
						boolean isDisabled = repoJson.get("isDisabled").asBoolean();
						boolean isEmpty = repoJson.get("isEmpty").asBoolean();
						boolean isLocked = repoJson.get("isLocked").asBoolean();
						String lastPush = repoJson.get("pushedAt").asText();
						int stars = repoJson.get("stargazerCount").asInt();
						String sshUrl = repoJson.get("sshUrl").asText();
						String url = repoJson.get("url").asText();
						boolean maven = repoJson.get("pom").hasNonNull("oid");
						boolean gradle = repoJson.get("gradle").hasNonNull("oid");

						if (maven && !isDisabled && !isEmpty && !isLocked) {
							Repository client = new Repository(owner, repo, stars,
								lastPush, sshUrl, url, maven, gradle, cursor);
							relevantClients.add(client);
							System.out.println("   %s:%s".formatted(owner, repo));
						}
					}
				}
			} catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return relevantClients;
	}

}
