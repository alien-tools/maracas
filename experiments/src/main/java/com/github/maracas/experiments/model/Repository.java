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
	 */
	private final boolean maven;

	/**
	 * Indicates if the repository has a Gradle nature
	 */
	private final boolean gradle;

	/**
	 * Number of total clients
	 */
	private int clients;

	/**
	 * List of relevant clients
	 */
	private List<Repository> relevantClients;


	/**
	 * Creates an instance of the {@link Repository} class. The number of clients
	 * of the repository is set to -1.
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
		this(owner, name, stars, lastPush, mergedPRs, maven, gradle, -1);
	}

	/**
	 * Creates an instance of the {@link Repository} class.
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
	 * @param clients   Number of clients
	 */
	public Repository(String owner, String name, int stars, LocalDate lastPush,
		int mergedPRs, boolean maven, boolean gradle, int clients) {
		this.owner = owner;
		this.name = name;
		this.stars = stars;
		this.lastPush = lastPush;
		this.mergedPRs = mergedPRs;
		this.maven = maven;
		this.gradle = gradle;
		this.clients = clients;
		this.relevantClients = new ArrayList<Repository>();
	}

	/**
	 * Creates an instance of the {@link Repository} class. The number of clients
	 * of the repository is set to -1.
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
		this(owner, name, stars, lastPush, mergedPRs, maven, gradle, -1);
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
	 * @param clients   Number of clients
	 */
	public Repository(String owner, String name, int stars, String lastPush,
		int mergedPRs, boolean maven, boolean gradle, int clients) {
		this(owner, name, stars,
			Date.from(Instant.parse(lastPush)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			mergedPRs, maven, gradle, clients);
	}

	/**
	 * Returns the number of total clients. Returns -1 if the field has not been
	 * modified since the creation of the {@link Repository} instance.
	 *
	 * @return number of clients
	 */
	public int getClients() {
		return clients;
	}

	/**
	 * Sets the number of total clients.
	 *
	 * @param clients Number of total clients
	 */
	public void setClients(int clients) {
		this.clients = clients;
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
	 * Returns the list of relevant clients of the repository.
	 *
	 * @return list of relevant clients of the repository
	 */
	public List<Repository> getRelevantClients() {
		return relevantClients;
	}

	/**
	 * Sets the list of relevant clients of the repository.
	 *
	 * @param relevantClients List of relevant clients of the repository
	 */
	public void setRelevantClients(List<Repository> relevantClients) {
		this.relevantClients = relevantClients;
	}

}
