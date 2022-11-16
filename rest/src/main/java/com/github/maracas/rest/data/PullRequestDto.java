package com.github.maracas.rest.data;

import com.github.maracas.forges.PullRequest;

public record PullRequestDto(
	String owner,
	String name,
	int number,
	String baseBranch,
	String headBranch,
	String baseSha,
	String headSha
) {
	public static PullRequestDto of(PullRequest pr) {
		return new PullRequestDto(
			pr.repository().owner(),
			pr.repository().name(),
			pr.number(),
			pr.baseBranch(),
			pr.headBranch(),
			pr.base().sha(),
			pr.head().sha()
		);
	}
}
