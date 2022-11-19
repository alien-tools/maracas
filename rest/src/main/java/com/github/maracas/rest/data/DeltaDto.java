package com.github.maracas.rest.data;

import com.github.maracas.forges.report.ForgeDelta;

import java.util.List;

public record DeltaDto(
	String jarV1,
	String jarV2,
	List<BreakingChangeDto> breakingChanges
) {
	public static DeltaDto of(ForgeDelta d) {
		return new DeltaDto(
			d.oldVersion().getJar().getFileName().toString(),
			d.newVersion().getJar().getFileName().toString(),
			d.breakingChanges().stream().map(BreakingChangeDto::of).toList()
		);
	}
}
