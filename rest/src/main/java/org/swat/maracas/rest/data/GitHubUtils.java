package org.swat.maracas.rest.data;

public class GitHubUtils {
	//e.g., https://github.com/tdegueul/comp-changes/blob/main/src/main/methodNoLongerStatic/MethodNoLongerStatic.java#L5-L7
	// FIXME: branches, etc.
	public static String buildGitHubUrl(String repo, String file, int beginLine, int endLine) {
		return String.format("https://github.com/%s/blob/main/%s#L%d-L%d",
			repo, file, beginLine, endLine);
	}
}
