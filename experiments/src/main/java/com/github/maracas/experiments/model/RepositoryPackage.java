package com.github.maracas.experiments.model;

import java.util.List;

/**
 * Represents a package of a {@link Repository}.
 */
public record RepositoryPackage(
	String name,
	Repository repository,
	int clients,
	List<Repository> relevantClients) {

	/**
	 * Creates a {@link RepositoryPackage} instance.
	 *
	 * @param name            Name of the package
	 * @param repository      Reference to the owning {@link Repository}
	 * @param clients         Total number of clients
	 * @param relevantClients List of relevant clients
	 */
	public RepositoryPackage(String name, Repository repository, int clients,
		List<Repository> relevantClients) {
		this.name = name;
		this.repository = repository;
		this.clients = clients;
		this.relevantClients = relevantClients;
	}
}
