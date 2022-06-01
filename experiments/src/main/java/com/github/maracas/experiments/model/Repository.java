package com.github.maracas.experiments.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * List of {@link RepositoryPackage} instances in the repository
	 */
	private List<RepositoryPackage> repoPackages;

	/**
	 * List of {@link PullRequest} instances of the repository
	 */
	private List<PullRequest> pullRequests;

	/**
	 * List of {@link Release} instances ordered by date
	 */
	private Map<String, Release> releases;

	/**
	 * Creates an instance of the {@link Repository} class. The {@code packages}
	 * and {@code pullRequests} fields are initialized as empty lists.
	 *
	 * @param owner     Owner of the repository
	 * @param name      Name of the repository
	 * @param stars     Number of stars of the repository
	 * @param lastPush  {@link LocalDate} of the last push
	 * @param mergedPRs Number of merged pulls requests
	 * @param maven     {@code true} if it is a Maven project, {@code false}
	 *                  otherwise
	 * @param gradle    {@code true} if it is a Gradle project, {@code false}
	 *                  otherwise
	 */
	public Repository(String owner, String name, int stars, LocalDate lastPush,
		boolean maven, boolean gradle) {
		this.owner = owner;
		this.name = name;
		this.stars = stars;
		this.lastPush = lastPush;
		this.maven = maven;
		this.gradle = gradle;
		this.repoPackages = new ArrayList<RepositoryPackage>();
		this.pullRequests = new ArrayList<PullRequest>();
		this.releases = new HashMap<String, Release>();
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
	 */
	public Repository(String owner, String name, int stars, String lastPush,
		boolean maven, boolean gradle) {
		this(owner, name, stars,
			Date.from(Instant.parse(lastPush)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			maven, gradle);
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
		if (pkg != null)
			repoPackages.add(pkg);
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
	 * Returns the list of {@link RepositoryPackage} instances of the repository.
	 *
	 * @return the list of {@link RepositoryPackage} instances
	 */
	public List<RepositoryPackage> getRepoPackages() {
		return repoPackages;
	}

	/**
	 * Sets the list of {@link RepositoryPackage} instances of the repository.
	 *
	 * @param packages List of {@link RepositoryPackage} instances
	 */
	public void setRepoPackages(List<RepositoryPackage> repoPackages) {
		this.repoPackages = repoPackages;
	}

	public Map<String, Release> getReleases() {
		return releases;
	}

	public void setReleases(Map<String, Release> releases) {
		this.releases = releases;
	}

	public boolean releaseExists(String version) {
		return releases.containsKey(version);
	}

	public Release getRelease(String version) {
		return releases.getOrDefault(version, null);
	}

	public void addRelease(Release release) {
		if (release != null)
			releases.putIfAbsent(release.getVersion(), release);
	}
}
