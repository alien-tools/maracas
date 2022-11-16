package com.github.maracas.rest.data;

import com.github.maracas.delta.Delta;
import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.List;

public record DeltaDto(
	String jarV1,
	String jarV2,
	List<BreakingChangeDto> breakingChanges
) {
	public static DeltaDto of(Delta d, PullRequest pr, Path clone) {
		return new DeltaDto(
			d.getOldVersion().getJar().getFileName().toString(),
			d.getNewVersion().getJar().getFileName().toString(),
			d.getBreakingChanges()
				.stream()
				.map(bc -> BreakingChangeDto.of(bc, pr, clone))
				.toList()
		);
	}
}
