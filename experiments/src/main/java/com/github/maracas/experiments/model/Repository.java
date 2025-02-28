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
	 * {@link LocalDate} of the creation of the repository
	 */
	private final LocalDate createdAt;

	/**
	 * {@link LocalDate} of the last push of the repository
	 */
	private final LocalDate pushedAt;

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

	private int clients;

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
	 * @param pushedAt  {@link LocalDate} of the last push
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
	public Repository(String owner, String name, int stars, LocalDate createdAt,
		LocalDate pushedAt, String sshUrl, String url, boolean maven, boolean gradle,
		String cursor) throws MalformedURLException, URISyntaxException {
		this.owner = owner;
		this.name = name;
		this.stars = stars;
		this.createdAt = createdAt;
		this.pushedAt = pushedAt;
		this.sshURL = new URI("ssh://" + sshUrl);
		this.url = new URL(url);
		this.maven = maven;
		this.gradle = gradle;
		this.cursor = cursor;
		this.pullRequests = new ArrayList<PullRequest>();
		this.gitHubPackages = -1;
		this.clients = -1;
	}

	/**
	 * Creates an instance of the {@link Repository} class.
	 *
	 * @param owner     Owner of the repository
	 * @param name      Name of the repository
	 * @param stars     Number of stars of the repository
	 * @param pushedAt  String representing the date of the last push
	 * @param mergedPRs Number of merged pulls requests
	 * @param maven     {@code true} if it is a Maven project, {@code false}
	 *                  otherwise
	 * @param gradle    {@code true} if it is a Gradle project, {@code false}
	 *                  otherwise
	 * @param cursor    Cursor returned by the GitHub API (used in case of errors)
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Repository(String owner, String name, int stars, String createdAt,
		String pushedAt, String sshUrl, String url, boolean maven, boolean gradle,
		String cursor) throws MalformedURLException, URISyntaxException {
		this(owner, name, stars,
			Date.from(Instant.parse(createdAt)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			Date.from(Instant.parse(pushedAt)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
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
	 * Adds a new pull request to the list of {@link PullRequest} instances of
	 * the repository.
	 *
	 * @param pkg {@link PullRequest} instance to add to the pull requests list
	 */
	public void addPullRequest(PullRequest pr) {
		if (pr != null)
			pullRequests.add(pr);
	}

	public int getGitHubPackages() {
		return gitHubPackages;
	}

	public void setGitHubPackages(int gitHubPackages) {
		this.gitHubPackages = gitHubPackages;
	}

	public int getClients() {
		return clients;
	}

	public void setClients(int clients) {
		this.clients = clients;
	}

	public URI getSshURL() {
		return sshURL;
	}

	/**
	 * Returns the {@link LocalDate} of the repository creation.
	 *
	 * @return the {@link LocalDate} of the repository creation
	 */
	public LocalDate getCreatedAt() {
		return createdAt;
	}

	/**
	 * Returns the {@link LocalDate} of the repository last push.
	 *
	 * @return the {@link LocalDate} of the repository last push
	 */
	public LocalDate getPushedAt() {
		return pushedAt;
	}
}
