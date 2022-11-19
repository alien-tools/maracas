package com.github.maracas.rest.data;

import com.github.maracas.forges.report.PullRequestReport;

import java.util.List;

public record PullRequestReportDto(
	List<PackageReportDto> packageReports,
	String error
) {
	public static PullRequestReportDto of(PullRequestReport report) {
		return new PullRequestReportDto(
			report.packageReports().stream().map(PackageReportDto::of).toList(),
			report.error()
		);
	}
}
