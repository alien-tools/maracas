package com.github.maracas.brokenuse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.delta.Delta;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * A delta impact lists the broken uses detected in a client project after computing
 * the delta model between two versions of a library. Broken uses are represented
 * as a set of {@link BrokenUse} instances.
 */
public class DeltaImpact {
	/**
	 * The client project
	 */
	private final SourcesDirectory client;

	/**
	 * The {@link Delta} model computed between two versions of the library
	 */
	private final Delta delta;

	/**
	 * The set of {@link BrokenUse} instances
	 */
	private final Set<BrokenUse> brokenUses;

	/**
	 * The {@link Throwable} we got in case the analysis failed
	 */
	private final Throwable throwable;

	/**
	 * Creates a {@link DeltaImpact} instance.
	 *
	 * @param client     the client project
	 * @param delta      the {@link Delta} model computed between two versions of a library
	 * @param brokenUses the set of computed {@link BrokenUse} instances
	 */
	public DeltaImpact(SourcesDirectory client, Delta delta, Set<BrokenUse> brokenUses) {
		this.client = client;
		this.delta = delta;
		this.brokenUses = brokenUses;
		this.throwable = null;
	}

	/**
	 * Creates a failed {@link DeltaImpact} instance.
	 *
	 * @param client    the client project
	 * @param delta     the {@link Delta} model computed between two versions of a library
	 * @param throwable the {@link Throwable} that was raised while attempting to compute broken uses
	 */
	public DeltaImpact(SourcesDirectory client, Delta delta, Throwable throwable) {
		this.client = client;
		this.delta = delta;
		this.brokenUses = Collections.emptySet();
		this.throwable = throwable;
	}

	/**
	 * Returns the client project
	 *
	 * @return the client project
	 */
	public SourcesDirectory getClient() {
		return client;
	}

	/**
	 * Returns the associated {@link Delta} model.
	 *
	 * @return the {@link Delta} model
	 */
	public Delta getDelta() {
		return delta;
	}

	/**
	 * Returns the set of {@link BrokenUse} instances.
	 *
	 * @return set of {@link BrokenUse} instances
	 */
	public Set<BrokenUse> getBrokenUses() {
		return brokenUses;
	}

	/**
	 * Returns the {@link Throwable} that was raised while attempting to compute broken uses, if any.
	 *
	 * @return the corresponding {@link Throwable}
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * Returns a JSON representation of the object.
	 *
	 * @return string with the JSON representation of the object
	 * @throws JsonProcessingException if jackson fails
	 */
	public String toJson() throws JsonProcessingException {
		return new ObjectMapper()
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this);
	}

	@Override
	public String toString() {
		return "ΔImpact(%s -> %s ON %s):%s)".formatted(
			delta.getOldVersion().getLabel(),
			delta.getNewVersion().getLabel(),
			client.getLocation(),
			throwable != null
				? throwable.getMessage()
				: brokenUses.stream()
					.map(bu -> "%n%s%n".formatted(bu.toString()))
					.collect(joining()));
	}
}
