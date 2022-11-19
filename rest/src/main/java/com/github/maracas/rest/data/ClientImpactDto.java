package com.github.maracas.rest.data;

import com.github.maracas.forges.report.ClientImpact;

import java.util.List;

public record ClientImpactDto(
	String owner,
	String name,
	String sha,
	List<BrokenUseDto> brokenUses,
	String error
) {
	public static ClientImpactDto of(ClientImpact impact) {
		return new ClientImpactDto(
			impact.client().repository().owner(),
			impact.client().repository().name(),
			impact.client().sha(),
			impact.brokenUses().stream().map(BrokenUseDto::of).toList(),
			impact.error()
		);
	}
}
