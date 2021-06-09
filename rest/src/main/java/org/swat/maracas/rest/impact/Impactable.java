package org.swat.maracas.rest.impact;

import java.util.concurrent.CompletableFuture;

import org.swat.maracas.rest.data.Delta;

// FIXME: Okay okay, that might be the worst interface ever
public interface Impactable {
	public Delta computeImpact(Delta delta);

	public default CompletableFuture<Delta> computeImpactAsync(Delta delta) {
		return CompletableFuture.supplyAsync(() -> computeImpact(delta));
	}
}
