package com.github.maracas.experiments.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	 * Number of merged pull requests
	 */
	private final int mergedPRs;

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
	 * List of {@link Package} instances in the repository
	 */
	private List<Package> packages;

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
	 * @param mergedPRs Number of merged pulls requests
	 * @param maven     {@code true} if it is a Maven project, {@code false}
	 *                  otherwise
	 * @param gradle    {@code true} if it is a Gradle project, {@code false}
	 *                  otherwise
	 */
	public Repository(String owner, String name, int stars, LocalDate lastPush,
		int mergedPRs, boolean maven, boolean gradle) {
		this.owner = owner;
		this.name = name;
		this.stars = stars;
		this.lastPush = lastPush;
		this.mergedPRs = mergedPRs;
		this.maven = maven;
		this.gradle = gradle;
		this.packages = new ArrayList<Package>();
		this.pullRequests = new ArrayList<PullRequest>();
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
		int mergedPRs, boolean maven, boolean gradle) {
		this(owner, name, stars,
			Date.from(Instant.parse(lastPush)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			mergedPRs, maven, gradle);
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
	 * Returns the number of merged pull requests.
	 *
	 * @return the number of merged pull requests
	 */
	public int getMergedPRs() {
		return mergedPRs;
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
	 * Returns the list of {@link Package} instances of the repository.
	 *
	 * @return the list of {@link Package} instances
	 */
	public List<Package> getPackages() {
		return packages;
	}

	/**
	 * Sets the list of {@link Package} instances of the repository.
	 *
	 * @param packages List of {@link Package} instances
	 */
	public void setPackages(List<Package> packages) {
		this.packages = packages;
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

}
