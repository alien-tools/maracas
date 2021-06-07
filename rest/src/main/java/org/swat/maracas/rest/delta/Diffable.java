package org.swat.maracas.rest.delta;

import java.util.concurrent.CompletableFuture;

import org.swat.maracas.rest.data.Delta;

public interface Diffable {
	public Delta diff();

	public default CompletableFuture<Delta> diffAsync() {
		return CompletableFuture.supplyAsync(() -> diff());
	}
}
