package com.github.maracas.rest.data;

import com.github.maracas.forges.report.PackageReport;

import java.util.Collections;
import java.util.List;

public record PackageReportDto(
	String id,
	String modulePath,
	DeltaDto delta,
	List<ClientImpactDto> clientReports,
	String error
) {
	public static PackageReportDto of(PackageReport report) {
		return new PackageReportDto(
			report.pkg().id(),
			report.pkg().modulePath().toString(),
			report.delta() != null ? DeltaDto.of(report.delta()) : null,
			report.clientsImpact().stream().map(ClientImpactDto::of).toList(),
			report.error()
		);
	}

	public List<BrokenUseDto> allBrokenUses() {
		return clientReports.stream().flatMap(c -> c.brokenUses().stream()).toList();
	}

	public List<BreakingChangeDto> impactfulBreakingChanges() {
		return delta.breakingChanges().stream()
			.filter(bc ->
				clientReports.stream()
					.flatMap(report -> report.brokenUses().stream())
					.anyMatch(bu -> bu.src().equals(bc.declaration()))
			)
			.toList();
	}
}
