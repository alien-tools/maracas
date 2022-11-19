package com.github.maracas.rest.data;

import com.github.maracas.forges.report.ForgeBreakingChange;
import com.github.maracas.util.SpoonHelpers;

public record BreakingChangeDto(
	String declaration,
	String change,
	String path,
	int startLine,
	int endLine,
	String fileUrl,
	String diffUrl
) {
	public static BreakingChangeDto of(ForgeBreakingChange bc) {
		return new BreakingChangeDto(
			SpoonHelpers.fullyQualifiedName(bc.breakingChange().getReference()),
			bc.breakingChange().getChange().name(),
			bc.path(),
			bc.startLine(),
			bc.endLine(),
			bc.fileUrl(),
			bc.diffUrl()
		);
	}
}
