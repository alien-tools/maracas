package com.github.maracas.rest.data;

import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.util.SpoonHelpers;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

import java.nio.file.Path;

public record BreakingChangeDto(
	String declaration,
	String change,
	String path,
	int startLine,
	int endLine,
	String fileUrl,
	String diffUrl
) {
	public static BreakingChangeDto of(BreakingChange decl, PullRequest pr, Path clone) {
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

		String relativeFile = clone.toAbsolutePath().relativize(Path.of(file).toAbsolutePath()).toString();
		return new BreakingChangeDto(
			SpoonHelpers.fullyQualifiedName(decl.getReference()),
			decl.getChange().name(),
			relativeFile,
			startLine,
			endLine,
			pr.repository().buildGitHubFileUrl(pr.baseBranch(), relativeFile, startLine, endLine),
			pr.buildGitHubDiffUrl(relativeFile, startLine)
		);
	}
}
