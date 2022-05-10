package com.github.maracas;

import com.github.maracas.util.PathHelpers;
import japicmp.util.Optional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An AnalysisQuery holds the information about a library's old and new
 * versions (JARs) and the clients (source code) to analyze.
 * <p>
 * Use the provided {@link AnalysisQuery.Builder} to build up analysis queries.
 */
public class AnalysisQuery {
	private final Path oldJar;
	private final Path newJar;
	private final Path sources;
	private final Collection<Path> clients;
	private final MaracasOptions options;

	/**
	 * Use the dedicated {@link Builder}
	 *
	 * @see #builder()
	 */
	private AnalysisQuery(Path oldJar, Path newJar, Path sources,
	                      Collection<Path> clients, MaracasOptions options) {
		this.oldJar = oldJar;
		this.newJar = newJar;
		this.sources = sources;
		this.clients = clients;
		this.options = options;
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
	 * Maracas options
	 *
	 * @see com.github.maracas.MaracasOptions
	 */
	public MaracasOptions getMaracasOptions() {
		return options;
	}

	/**
	 * AnalysisQuery's builder.
	 * <p>
	 * Only {@link #oldJar(Path)} and {@link #newJar(Path)} are mandatory.
	 */
	public static class Builder {
		private Path oldJar;
		private Path newJar;
		private Path sources;
		private final Collection<Path> clients = new ArrayList<>();
		private MaracasOptions options = MaracasOptions.newDefault();

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
			if (!PathHelpers.isValidJar(oldJar))
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
			if (!PathHelpers.isValidJar(newJar))
				throw new IllegalArgumentException("newJar isn't a valid JAR: " + newJar);

			this.newJar = newJar.toAbsolutePath();
			return this;
		}

		/**
		 * Sets the library's source code. Must point to a Maven project (ie. contains a {@code pom.xml}).
		 *
		 * @param sources Valid path to the directory containing the source code
		 *                of the old version of the library.
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder sources(Path sources) {
			if (!PathHelpers.isValidMavenDirectory(sources))
				throw new IllegalArgumentException("sources isn't a valid Maven directory: " + sources);

			this.sources = sources.toAbsolutePath();
			return this;
		}

		/**
		 * Includes a client into the analysis. Must point to a Maven project (ie. contains a {@code pom.xml}).
		 *
		 * @param client Valid path to the directory containing the source code of
		 *               a client
		 * @return the builder
		 * @throws IllegalArgumentException if the path isn't valid
		 */
		public Builder client(Path client) {
			if (!PathHelpers.isValidMavenDirectory(client))
				throw new IllegalArgumentException("client isn't a valid Maven directory: " + client);

			if (!this.clients.contains(client.toAbsolutePath()))
				this.clients.add(client.toAbsolutePath());
			return this;
		}

		/**
		 * Includes a set of clients into the analysis.
		 *
		 * @return the builder
		 * @throws IllegalArgumentException if clients is null
		 * @see #client(Path)
		 */
		public Builder clients(Collection<Path> clients) {
			if (clients == null)
				throw new IllegalArgumentException("clients is null");

			clients.forEach(this::client);
			return this;
		}

		/**
		 * Set the options to pass to Maracas
		 *
		 * @param options Maracas options
		 * @return the builder
		 * @see com.github.maracas.MaracasOptions
		 */
		public Builder options(MaracasOptions options) {
			if (options == null)
				throw new IllegalArgumentException("options is null");

			this.options = options;
			return this;
		}

		/**
		 * Excludes some part of the API from the analysis
		 *
		 * @param pattern The pattern to exclude ("@Annotation", "*package*", etc.)
		 * @return the builder
		 * @see japicmp.config.Options#addExcludeFromArgument(japicmp.util.Optional, boolean)
		 */
		public Builder exclude(String pattern) {
			if (pattern == null)
				throw new IllegalArgumentException("pattern is null");

			this.options.getJApiOptions().addExcludeFromArgument(Optional.of(pattern), false);
			return this;
		}

		/**
		 * Builds the {@link AnalysisQuery} object and returns it.
		 *
		 * @return the final query
		 */
		public AnalysisQuery build() {
			validate();
			return new AnalysisQuery(oldJar, newJar, sources, clients, options);
		}

		private void validate() {
			if (oldJar == null || newJar == null)
				throw new IllegalStateException("oldJar and newJar must be supplied");
		}
	}
}
