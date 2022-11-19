package com.github.maracas.forges.report;

import com.github.maracas.LibraryJar;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.List;

public record ForgeDelta(
	LibraryJar oldVersion,
	LibraryJar newVersion,
	List<ForgeBreakingChange> breakingChanges
) {
	public static ForgeDelta of(Delta d, PullRequest pr, Path clone) {
		return new ForgeDelta(
			d.getOldVersion(),
			d.getNewVersion(),
			d.getBreakingChanges()
				.stream()
				.map(bc -> ForgeBreakingChange.of(bc, pr, clone))
				.toList()
		);
	}

	public static ForgeDelta of(Delta d, Commit v1, Commit v2, Path clone) {
		return new ForgeDelta(
			d.getOldVersion(),
			d.getNewVersion(),
			d.getBreakingChanges()
				.stream()
				.map(bc -> ForgeBreakingChange.of(bc, v1, v2, clone))
				.toList()
		);
	}
}
