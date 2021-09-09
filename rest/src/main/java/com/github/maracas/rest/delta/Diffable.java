package com.github.maracas.rest.delta;

import java.util.concurrent.CompletableFuture;

import com.github.maracas.rest.data.MaracasReport;

public interface Diffable {
	public abstract MaracasReport diff();

	public default CompletableFuture<MaracasReport> diffAsync() {
		return CompletableFuture.supplyAsync(this::diff);
	}
}
