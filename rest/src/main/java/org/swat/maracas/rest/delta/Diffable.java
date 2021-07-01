package org.swat.maracas.rest.delta;

import java.util.concurrent.CompletableFuture;

import org.swat.maracas.rest.data.MaracasReport;

public interface Diffable {
	public MaracasReport diff();

	public default CompletableFuture<MaracasReport> diffAsync() {
		return CompletableFuture.supplyAsync(this::diff);
	}
}
