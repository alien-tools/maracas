package com.github.maracas.forges.report;

import com.github.maracas.forges.PullRequest;

import java.util.Collections;
import java.util.List;

public record PullRequestReport(
	PullRequest pr,
	List<PackageReport> packageReports,
	String error
) {
	public static PullRequestReport error(PullRequest pr, String error) {
		return new PullRequestReport(pr, Collections.emptyList(), error);
	}

	public static PullRequestReport success(PullRequest pr, List<PackageReport> packageReports) {
		return new PullRequestReport(pr, packageReports, null);
	}
}
