package com.github.maracas.rest.data;

import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.analysis.PullRequestAnalysisResult;

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
					DeltaDto.of(pkg.delta(), result.pr(), result.basePath()),
					pkg.clientResults().entrySet()
						.stream()
						.map(r -> {
							Repository client = r.getKey();
							DeltaImpact impact = r.getValue();

							return ClientReport.success(
								client.githubWebUrl(),
								impact.getBrokenUses()
									.stream()
									.map(bu -> BrokenUseDto.of(bu, client, client.branch(), impact.getClient().getLocation()))
									.toList()
							);
						})
						.toList()
				))
				.toList()
		);
	}
}
