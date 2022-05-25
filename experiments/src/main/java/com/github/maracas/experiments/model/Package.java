package com.github.maracas.experiments.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a package of a {@link Repository}.
 */
public class Package {
	/**
	 * Name of the package
	 */
	private final String name;

	/**
	 * Repository of the package
	 */
	private final Repository repository;

	/**
	 * Number of total clients
	 */
	private int clients;

	/**
	 * List of relevant clients
	 */
	private List<Repository> relevantClients;

	/**
	 * Creates a {@link Package} instance. The total number is initialized to -1.
	 *
	 * @param name       name of the package
	 * @param repository repository of the package
	 */
	public Package(String name, Repository repository) {
		this.name = name;
		this.repository = repository;
		this.clients = -1;
		this.relevantClients = new ArrayList<Repository>();
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
	 * Returns the name of the package.
	 *
	 * @return name of the package
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the repository of the package.
	 *
	 * @return repository of the package
	 */
	public Repository getRepository() {
		return repository;
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
