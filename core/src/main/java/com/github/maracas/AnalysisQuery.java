package com.github.maracas;

import japicmp.util.Optional;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An AnalysisQuery holds the information about a library's old and new
 * versions, the clients to analyze, and user-defined analysis options.
 * <p>
 * Use the provided {@link AnalysisQuery.Builder} to build up analysis queries.
 */
public class AnalysisQuery {
	private final LibraryJar oldVersion;
	private final LibraryJar newVersion;
	private final Collection<SourcesDirectory> clients;
	private final MaracasOptions options;

	/**
	 * Use the dedicated {@link Builder}
	 *
	 * @see #builder()
	 */
	private AnalysisQuery(LibraryJar oldVersion, LibraryJar newVersion,
	                      Collection<SourcesDirectory> clients, MaracasOptions options) {
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
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
	 * The library's old version
	 */
	public LibraryJar getOldVersion() {
		return oldVersion;
	}

	/**
	 * The library's new JAR
	 */
	public LibraryJar getNewVersion() {
		return newVersion;
	}

	/**
	 * The clients
	 */
	public Collection<SourcesDirectory> getClients() {
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
	 * Only {@link #oldVersion(LibraryJar)} and {@link #newVersion(LibraryJar)} are mandatory.
	 */
	public static class Builder {
		private LibraryJar oldVersion;
		private LibraryJar newVersion;
		private final Collection<SourcesDirectory> clients = new ArrayList<>();
		private MaracasOptions options = MaracasOptions.newDefault();

		/**
		 * Use {@link AnalysisQuery#builder()}
		 */
		private Builder() {

		}

		/**
		 * Sets the library's old version. Required.
		 *
		 * @param oldVersion The library's old version
		 * @return the builder
		 * @throws IllegalArgumentException if the library is null
		 */
		public Builder oldVersion(LibraryJar oldVersion) {
			if (oldVersion == null)
				throw new IllegalArgumentException("oldVersion is null");

			this.oldVersion = oldVersion;
			return this;
		}

		/**
		 * Sets the library's new version. Required.
		 *
		 * @param newVersion The library's new version
		 * @return the builder
		 * @throws IllegalArgumentException if the library is null
		 */
		public Builder newVersion(LibraryJar newVersion) {
			if (newVersion == null)
				throw new IllegalArgumentException("newVersion is null");

			this.newVersion = newVersion;
			return this;
		}

		/**
		 * Includes a client into the analysis.
		 *
		 * @param client A client to analyze
		 * @return the builder
		 * @throws IllegalArgumentException if the client is null
		 */
		public Builder client(SourcesDirectory client) {
			if (client == null)
				throw new IllegalArgumentException("client is null");

			if (!this.clients.contains(client))
				this.clients.add(client);
			return this;
		}

		/**
		 * Includes a set of clients into the analysis.
		 *
		 * @return the builder
		 * @throws IllegalArgumentException if clients is null
		 * @see #client(SourcesDirectory)
		 */
		public Builder clients(Collection<SourcesDirectory> clients) {
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
			return new AnalysisQuery(oldVersion, newVersion, clients, options);
		}

		private void validate() {
			if (oldVersion == null || newVersion == null)
				throw new IllegalStateException("oldVersion and newVersion must be supplied");
		}
	}
}
