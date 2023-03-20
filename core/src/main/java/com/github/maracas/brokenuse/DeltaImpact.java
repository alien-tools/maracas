package com.github.maracas.brokenuse;

import com.github.maracas.SourcesDirectory;
import com.github.maracas.delta.Delta;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A delta impact lists the broken uses detected in a client project after computing
 * the delta model between two versions of a library. Broken uses are represented
 * as a set of {@link BrokenUse} instances.
 *
 * @param client     the analyzed client
 * @param delta      the potentially impacting {@link Delta} model
 * @param brokenUses the set of {@link BrokenUse} detected in the client
 * @param throwable  the {@link Throwable} we got in case the analysis failed
 */
public record DeltaImpact(
	SourcesDirectory client,
	Delta delta,
	Set<BrokenUse> brokenUses,
	Throwable throwable
) {
	public DeltaImpact {
		Objects.requireNonNull(client);
		Objects.requireNonNull(delta);
		Objects.requireNonNull(brokenUses);
	}

	public static DeltaImpact success(SourcesDirectory client, Delta delta, Set<BrokenUse> brokenUses) {
		return new DeltaImpact(client, delta, brokenUses, null);
	}

	public static DeltaImpact error(SourcesDirectory client, Delta delta, Throwable throwable) {
		return new DeltaImpact(client, delta, Collections.emptySet(), throwable);
	}
}
