package com.github.maracas.rest.data;

import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.List;

public record Delta(
	Path jarV1,
	Path jarV2,
	List<BreakingChange> breakingChanges
) {
	public static Delta fromMaracasDelta(com.github.maracas.delta.Delta d, PullRequest pr, Path clone) {
		return new Delta(
			d.getOldJar(),
			d.getNewJar(),
			d.getBreakingChanges()
				.stream()
				.map(bc -> BreakingChange.fromMaracasBreakingChange(bc, pr, clone))
				.toList()
		);
	}
}
