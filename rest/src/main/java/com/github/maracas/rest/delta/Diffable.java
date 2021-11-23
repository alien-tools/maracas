package com.github.maracas.rest.delta;

import com.github.maracas.rest.data.MaracasReport;

import java.util.concurrent.CompletableFuture;

public interface Diffable {
	MaracasReport diff();
	default CompletableFuture<MaracasReport> diffAsync() {
		return CompletableFuture.supplyAsync(this::diff);
	}
}
