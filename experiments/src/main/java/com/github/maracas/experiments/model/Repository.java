package com.github.maracas.experiments.model;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a GitHub repository. Includes the fields required for the
 * experiment analysis.
 */
public class Repository {
	/**
	 * Owner of the repository
	 */
	private final String owner;

	/**
	 * Name of the repository
	 */
	private final String name;

	/**
	 * Number of stars of the repository
	 */
	private final int stars;

	/**
	 * {@link LocalDate} of the last push of the repository
	 */
	private final LocalDate lastPush;

	private final URI sshURL;

	private final URL url;

	/**
	 * Indicates if the repository has a Maven nature
	 * TODO: move to Package?
	 */
	private final boolean maven;

	/**
	 * Indicates if the repository has a Gradle nature
	 * TODO: move to Package?
	 */
	private final boolean gradle;

	private final String cursor;

	private List<RepositoryPackage> repoPackages;

	/**
	 * Map of {@link RepositoryPackage} instances in the repository. Keys are
	 * the combination of the object ID and the artifact ID (i.e. objectID:artifactID)
	 */
	private Map<String, RepositoryPackage> repoPackagesByName;

	private Map<String, RepositoryPackage> repoPackagesByPath;

	private int gitHubPackages;

	/**
	 * List of {@link PullRequest} instances of the repository
	 */
	private List<PullRequest> pullRequests;


	/**
	 * Creates an instance of the {@link Repository} class. The {@code packages}
	 * and {@code pullRequests} fields are initialized as empty lists.
	 *
	 * @param owner     Owner of the repository
	 * @param name      Name of the repository
	 * @param stars     Number of stars of the repository
	 * @param lastPush  {@link LocalDate} of the last push
	 * @param sshUrl    SSH URL to clone the repository
	 * @param url       HTTP URL to clone the repository
	 * @param mergedPRs Number of merged pulls requests
	 * @param maven     {@code true} if it is a Maven project, {@code false}
	 *                  otherwise
	 * @param gradle    {@code true} if it is a Gradle project, {@code false}
	 *                  otherwise
	 * @param cursor    Cursor returned by the GitHub API (used in case of errors)
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Repository(String owner, String name, int stars, LocalDate lastPush,
		String sshUrl, String url, boolean maven, boolean gradle, String cursor) throws MalformedURLException, URISyntaxException {
		this.owner = owner;
		this.name = name;
		this.stars = stars;
		this.lastPush = lastPush;
		this.sshURL = new URI("ssh://" + sshUrl);
		this.url = new URL(url);
		this.maven = maven;
		this.gradle = gradle;
		this.cursor = cursor;
		this.repoPackages = new ArrayList<RepositoryPackage>();
		this.repoPackagesByName = new HashMap<String, RepositoryPackage>();
		this.repoPackagesByPath = new HashMap<String, RepositoryPackage>();
		this.pullRequests = new ArrayList<PullRequest>();
		this.gitHubPackages = -1;
	}

	/**
	 * Creates an instance of the {@link Repository} class.
	 *
	 * @param owner     Owner of the repository
	 * @param name      Name of the repository
	 * @param stars     Number of stars of the repository
	 * @param lastPush  String representing the date of the last push
	 * @param mergedPRs Number of merged pulls requests
	 * @param maven     {@code true} if it is a Maven project, {@code false}
	 *                  otherwise
	 * @param gradle    {@code true} if it is a Gradle project, {@code false}
	 *                  otherwise
	 * @param cursor    Cursor returned by the GitHub API (used in case of errors)
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Repository(String owner, String name, int stars, String lastPush,
		String sshUrl, String url, boolean maven, boolean gradle, String cursor) throws MalformedURLException, URISyntaxException {
		this(owner, name, stars,
			Date.from(Instant.parse(lastPush)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			sshUrl, url, maven, gradle, cursor);
	}

	/**
	 * Returns the owner of the repository.
	 *
	 * @return the owner of the repository
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Name of the repository.
	 *
	 * @return the name of the repository
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the number of stars of the repository.
	 *
	 * @return the number of stars of the repository
	 */
	public int getStars() {
		return stars;
	}

	/**
	 * Returns the {@link LocalDate} of the repository last push.
	 *
	 * @return the {@link LocalDate} of the repository last push
	 */
	public LocalDate getLastPush() {
		return lastPush;
	}

	public URI getSshUrl() {
		return sshURL;
	}

	public URL getUrl() {
		return url;
	}

	/**
	 * Indicates if the repository has a Maven nature.
	 *
	 * @return {@code true} if the repository has a Maven nature, {@code false}
	 *         otherwise
	 */
	public boolean isMaven() {
		return maven;
	}

	/**
	 * Indicates if the repository has a Gradle nature.
	 *
	 * @return {@code true} if the repository has a Gradle nature, {@code false}
	 *         otherwise
	 */
	public boolean isGradle() {
		return gradle;
	}

	public String getCursor() {
		return cursor;
	}

	/**
	 * Returns the list of {@link PullRequest} instances of the repository.
	 *
	 * @return the list of {@link PullRequest} instances
	 */
	public List<PullRequest> getPullRequests() {
		return pullRequests;
	}

	/**
	 * Sets the list of {@link PullRequest} instances of the repository.
	 *
	 * @param pullRequests List of {@link PullRequest} instances of the repository
	 */
	public void setPullRequests(List<PullRequest> pullRequests) {
		this.pullRequests = pullRequests;
	}

	/**
	 * Adds a new package to the list of {@link RepositoryPackage} instances of
	 * the repository.
	 *
	 * @param pkg {@link RepositoryPackage} instance to add to the packages list
	 */
	public void addPackage(RepositoryPackage pkg) {
		if (pkg != null) {
			repoPackagesByName.put(pkg.getName(), pkg);
			repoPackagesByPath.put(pkg.getRelativePath(), pkg);
		}
	}

	/**
	 * Adds a new pull request to the list of {@link PullRequest} instances of
	 * the repository.
	 *
	 * @param pkg {@link PullRequest} instance to add to the pull requests list
	 */
	public void addPullRequest(PullRequest pr) {
		if (pr != null)
			pullRequests.add(pr);
	}

	/**
	 * Returns the map of {@link RepositoryPackage} instances of the repository.
	 *
	 * @return the map of {@link RepositoryPackage} instances
	 */
	public Map<String, RepositoryPackage> getRepoPackagesByName() {
		return repoPackagesByName;
	}

	public RepositoryPackage getRepoPackageByName(String name) {
		return repoPackagesByName.getOrDefault(name, null);
	}

	public void setRepoPackagesByPath(Map<String, RepositoryPackage> repoPackages) {
		this.repoPackagesByPath = repoPackages;
	}

	public Map<String, RepositoryPackage> getRepoPackagesByPath() {
		return repoPackagesByPath;
	}

	public RepositoryPackage getRepoPackageByPath(String path) {
		return repoPackagesByPath.getOrDefault(path, null);
	}

	/**
	 * Sets the map of {@link RepositoryPackage} instances of the repository.
	 *
	 * @param packages Map of {@link RepositoryPackage} instances
	 */
	public void setRepoPackagesByName(Map<String, RepositoryPackage> repoPackages) {
		this.repoPackagesByName = repoPackages;
	}

	public int getGitHubPackages() {
		return gitHubPackages;
	}

	public void setGitHubPackages(int gitHubPackages) {
		this.gitHubPackages = gitHubPackages;
	}

	public URI getSshURL() {
		return sshURL;
	}

	public List<RepositoryPackage> getRepoPackages() {
		return repoPackagesByName.values().stream().collect(Collectors.toList());
	}
}
