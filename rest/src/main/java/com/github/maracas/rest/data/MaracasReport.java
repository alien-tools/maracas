package com.github.maracas.rest.data;

import com.github.maracas.forges.analysis.PullRequestAnalysisResult;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public record MaracasReport(
	List<PackageReport> reports
) {
	public static MaracasReport of(PullRequestAnalysisResult result) {
		return new MaracasReport(
			result.packageResults().values()
				.stream()
				.map(pkg -> new PackageReport(
					pkg.pkdId(),
					pkg.error(),
					DeltaDto.of(pkg.delta(), result.pr(), Path.of("") /* FIXME */),
					Collections.emptyList()
				))
				.toList()
		);
	}
}
