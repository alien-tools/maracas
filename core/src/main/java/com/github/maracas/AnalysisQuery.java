package com.github.maracas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An AnalysisQuery holds the information about a library's old and new
 * versions (JARs) and the clients (source code) to analyze.
 *
 * Use the provided {@link AnalysisQuery.Builder} to build up analysis queries.
 */
public class AnalysisQuery {
	private final Path oldJar;
	private final Path newJar;
	private final Path sources;
	private final Collection<Path> clients;

	/**
	 * Use the dedicated {@link Builder}
	 *
	 * @see #builder()
	 */
	private AnalysisQuery(Path oldJar, Path newJar, Path sources, Collection<Path> clients) {
		this.oldJar = oldJar;
		this.newJar = newJar;
		this.sources = sources;
		this.clients = clients;
	}

	/**
	 * Returns a query builder
	 *
	 * @see Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * The library's old JAR
	 */
	public Path getOldJar() {
		return oldJar;
	}

	/**
	 * The library's new JAR
	 */
	public Path getNewJar() {
		return newJar;
	}

	/**
	 * The old library's source code directory
	 */
	public Path getSources() {
		return sources;
	}

	/**
	 * The clients' source code directories
	 */
	public Collection<Path> getClients() {
		return clients;
	}

	/**
	 * AnalysisQuery's builder.
	 *
	 * Only {@link #oldJar(Path)} and {@link #newJar(Path)} are mandatory.
	 */
	public static class Builder {
		private Path oldJar;
		private Path newJar;
		private Path sources;
		private Collection<Path> clients = new ArrayList<>();

		/**
		 * Use {@link AnalysisQuery#builder()}
		 */
		private Builder() {

		}

		/**
		 * Sets the library's old JAR. Required.
		 *
		 * @param oldJar Valid path to the library's old JAR
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder oldJar(Path oldJar) {
			if (!isValidJar(oldJar))
				throw new IllegalArgumentException("oldJar isn't a valid JAR: " + oldJar);

			this.oldJar = oldJar.toAbsolutePath();
			return this;
		}

		/**
		 * Sets the library's new JAR. Required.
		 *
		 * @param newJar Valid path to the library's new JAR
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder newJar(Path newJar) {
			if (!isValidJar(newJar))
				throw new IllegalArgumentException("newJar isn't a valid JAR: " + newJar);

			this.newJar = newJar.toAbsolutePath();
			return this;
		}

		/**
		 * Sets the library's source code.
		 *
		 * @param sources Valid path to the directory containing the source code
		 * of the old version of the library.
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder sources(Path sources) {
			if (!isValidDirectory(sources))
				throw new IllegalArgumentException("sources isn't a valid directory: " + sources);

			this.sources = sources.toAbsolutePath();
			return this;
		}

		/**
		 * Includes a client into the analysis.
		 *
		 * @param client Valid path to the directory containing the source code of
		 * a client
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder client(Path client) {
			if (!isValidDirectory(client))
				throw new IllegalArgumentException("client isn't a valid directory: " + client);

			if (!this.clients.contains(client.toAbsolutePath()))
				this.clients.add(client.toAbsolutePath());
			return this;
		}

		/**
		 * Includes a set of clients into the analysis.
		 *
		 * @see #client(Path)
		 */
		public Builder clients(Collection<Path> clients) {
			if (clients == null)
				throw new IllegalArgumentException("clients is null");

			clients.forEach(this::client);
			return this;
		}

		/**
		 * Builds the {@link AnalysisQuery} object and returns it.
		 *
		 * @return the final query
		 */
		public AnalysisQuery build() {
			validate();
			return new AnalysisQuery(oldJar, newJar, sources, clients);
		}

		private void validate() {
			if (oldJar == null || newJar == null)
				throw new IllegalStateException("oldJar and newJar must be supplied");
		}

		private boolean isValidJar(Path p) {
			return
				p != null &&
				Files.exists(p) &&
				Files.isRegularFile(p) &&
				p.getFileName().toString().endsWith(".jar");
		}

		private boolean isValidDirectory(Path p) {
			return
				p != null &&
				Files.exists(p) &&
				Files.isDirectory(p);
		}
	}
}
