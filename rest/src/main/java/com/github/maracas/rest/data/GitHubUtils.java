package com.github.maracas.rest.data;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class GitHubUtils {
	private GitHubUtils() {}

	//e.g., https://github.com/alien-tools/comp-changes/blob/main/src/main/methodNoLongerStatic/MethodNoLongerStatic.java#L5-L7
	public static String buildGitHubFileUrl(String owner, String repo, String ref, String file, int beginLine, int endLine) {
		return "https://github.com/%s/%s/blob/%s/%s#L%d-L%d".formatted(owner, repo, ref, file, beginLine, endLine);
	}

	public static String buildGitHubDiffUrl(String owner, String repo, int prId, String file, int beginLine) {
		return "https://github.com/%s/%s/pull/%d/files#diff-%sL%d".formatted(
			owner, repo, prId, Hashing.sha256().hashString(file, StandardCharsets.UTF_8), beginLine);
	}
}
