package com.github.maracas.rest.data;

import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.analysis.PullRequestAnalysisResult;

import java.util.List;

public record MaracasReport(
	List<ModuleReport> reports
) {
	public static MaracasReport of(PullRequestAnalysisResult result) {
		return new MaracasReport(
			result.moduleResults().values()
				.stream()
				.map(module -> new ModuleReport(
					module.moduleId(),
					module.error(),
					module.delta() != null ? DeltaDto.of(module.delta(), result.pr(), module.basePath()) : null,
					module.clientResults().entrySet()
						.stream()
						.map(r -> {
							Repository client = r.getKey();
							DeltaImpact impact = r.getValue();

							return ClientReport.success(
								client.fullName(),
								client.githubWebUrl(),
								impact.brokenUses()
									.stream()
									.map(bu -> BrokenUseDto.of(bu, client, client.branch(), impact.client().getLocation()))
									.toList()
							);
						})
						.toList()
				))
				.toList()
		);
	}
}
