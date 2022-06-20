package com.github.maracas.experiments.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a package of a {@link Repository}.
 */
public class RepositoryPackage {
	/**
	 * Name of the package as it appear on the dependency graph
	 * (i.e groupID:artifactID)
	 */
	private final String name;

	/**
	 * Artifact ID of the package as defined in the POM file
	 */
	private final String artifact;

	/**
	 * Group ID of the package as defined in the POM file
	 */
	private final String group;

	/**
	 * Current version of the package as defined in the POM file
	 */
	private final String version;

	/**
	 * Relative path with respect to the repository content
	 */
	private final String relativePath;

	/**
	 * Reference to the owning {@link Repository}
	 */
	private final Repository repository;

	/**
	 * Total number of clients as it appear on the dependency graph
	 */
	private int clients;

	/**
	 * List of relevant client {@link Repository} instances
	 */
	private List<Repository> relevantClients;

	/**
	 * Creates a {@link RepositoryPackage} instance.
	 *
	 * @param name            Name of the package
	 * @param repository      Reference to the owning {@link Repository}
	 * @param clients         Total number of clients
	 * @param relevantClients List of relevant clients
	 */
	public RepositoryPackage(String group, String artifact, String currentVersion,
		String relativePath, Repository repository) {
		this.name = "%s:%s".formatted(group, artifact);
		this.group = group;
		this.artifact = artifact;
		this.version = currentVersion;
		this.relativePath = relativePath;
		this.repository = repository;
		this.clients = 0;
		this.relevantClients = new ArrayList<Repository>();
	}

	public String getName() {
		return name;
	}

	public String getArtifact() {
		return artifact;
	}

	public String getGroup() {
		return group;
	}

	public String getVersion() {
		return version;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setClients(int clients) {
		this.clients = clients;
	}

	public int getClients() {
		return clients;
	}

	public void setRelevantClients(List<Repository> relevantClients) {
		this.relevantClients = relevantClients;
	}

	public List<Repository> getRelevantClients() {
		return relevantClients;
	}
}
