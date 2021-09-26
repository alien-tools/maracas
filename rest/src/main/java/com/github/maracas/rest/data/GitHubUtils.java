package com.github.maracas.rest.data;

public class GitHubUtils {
	private GitHubUtils() {}

	//e.g., https://github.com/tdegueul/comp-changes/blob/main/src/main/methodNoLongerStatic/MethodNoLongerStatic.java#L5-L7
	public static String buildGitHubUrl(String repo, String ref, String file, int beginLine, int endLine) {
		return String.format("https://github.com/%s/blob/%s/%s#L%d-L%d",
			repo, ref, file, beginLine, endLine);
	}
}
