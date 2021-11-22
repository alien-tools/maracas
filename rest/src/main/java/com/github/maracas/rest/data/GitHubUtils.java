package com.github.maracas.rest.data;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class GitHubUtils {
	private GitHubUtils() {}

	//e.g., https://github.com/alien-tools/comp-changes/blob/main/src/main/methodNoLongerStatic/MethodNoLongerStatic.java#L5-L7
	public static String buildGitHubFileUrl(String repo, String ref, String file, int beginLine, int endLine) {
		return "https://github.com/%s/blob/%s/%s#L%d-L%d".formatted(repo, ref, file, beginLine, endLine);
	}

	public static String buildGitHubDiffUrl(String repo, int prId, String file, int beginLine) {
		return "https://github.com/%s/pull/%d/files#diff-%sL%d".formatted(
			repo, prId, Hashing.sha256().hashString(file, StandardCharsets.UTF_8), beginLine);
	}
}
