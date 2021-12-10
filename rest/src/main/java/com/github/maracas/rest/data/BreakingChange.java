package com.github.maracas.rest.data;

import com.github.maracas.rest.util.GitHubUtils;
import com.github.maracas.util.SpoonHelpers;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

import java.nio.file.Paths;

public record BreakingChange(
	String declaration,
	String change,
	String path,
	int startLine,
	int endLine,
	String fileUrl,
	String diffUrl
) {
	public static BreakingChange fromMaracasBreakingChange(com.github.maracas.delta.BreakingChange decl, PullRequest pr, String ref, String clonePath) {
		String file = "";
		int startLine = -1;
		int endLine = -1;

		if (decl.getSourceElement() != null) {
			SourcePosition pos = decl.getSourceElement().getPosition();

			if (pos != null) {
				file = pos instanceof NoSourcePosition ? "" : pos.getFile().getAbsolutePath();
				startLine = pos instanceof NoSourcePosition ? -1 : pos.getLine();
				endLine = pos instanceof NoSourcePosition ? -1 : pos.getEndLine();
			}
		}

		String relativeFile = Paths.get(clonePath).toAbsolutePath().relativize(Paths.get(file).toAbsolutePath()).toString();
		return new BreakingChange(
			SpoonHelpers.fullyQualifiedName(decl.getReference()),
			decl.getChange().name(),
			relativeFile,
			startLine,
			endLine,
			GitHubUtils.buildGitHubFileUrl(pr.owner(), pr.repository(), ref, relativeFile, startLine, endLine),
			GitHubUtils.buildGitHubDiffUrl(pr.owner(), pr.repository(), pr.id(), relativeFile, startLine)
		);
	}
}
