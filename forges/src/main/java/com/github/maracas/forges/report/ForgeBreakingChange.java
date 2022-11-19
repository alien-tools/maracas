package com.github.maracas.forges.report;

import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.PullRequest;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

import java.nio.file.Path;

public record ForgeBreakingChange(
	BreakingChange breakingChange,
	String path,
	int startLine,
	int endLine,
	String fileUrl,
	String diffUrl
) {
	public static ForgeBreakingChange of(BreakingChange bc, PullRequest pr, Path clone) {
		String file = "";
		int startLine = -1;
		int endLine = -1;

		if (bc.getSourceElement() != null) {
			SourcePosition pos = bc.getSourceElement().getPosition();

			if (pos != null) {
				file = pos instanceof NoSourcePosition ? "" : pos.getFile().getAbsolutePath();
				startLine = pos instanceof NoSourcePosition ? -1 : pos.getLine();
				endLine = pos instanceof NoSourcePosition ? -1 : pos.getEndLine();
			}
		}

		String relativeFile = clone.toAbsolutePath().relativize(Path.of(file).toAbsolutePath()).toString();
		return new ForgeBreakingChange(
			bc,
			relativeFile,
			startLine,
			endLine,
			pr.repository().buildGitHubFileUrl(pr.baseBranch(), relativeFile, startLine, endLine),
			pr.buildGitHubDiffUrl(relativeFile, startLine, endLine)
		);
	}

	public static ForgeBreakingChange of(BreakingChange bc, Commit v1, Commit v2, Path clone) {
		String file = "";
		int startLine = -1;
		int endLine = -1;

		if (bc.getSourceElement() != null) {
			SourcePosition pos = bc.getSourceElement().getPosition();

			if (pos != null) {
				file = pos instanceof NoSourcePosition ? "" : pos.getFile().getAbsolutePath();
				startLine = pos instanceof NoSourcePosition ? -1 : pos.getLine();
				endLine = pos instanceof NoSourcePosition ? -1 : pos.getEndLine();
			}
		}

		String relativeFile = clone.toAbsolutePath().relativize(Path.of(file).toAbsolutePath()).toString();
		return new ForgeBreakingChange(
			bc,
			relativeFile,
			startLine,
			endLine,
			v1.repository().buildGitHubFileUrl(v1.repository().branch(), relativeFile, startLine, endLine),
			v1.repository().buildGitHubDiffUrl(v1, v2, relativeFile, startLine, endLine)
		);
	}
}
