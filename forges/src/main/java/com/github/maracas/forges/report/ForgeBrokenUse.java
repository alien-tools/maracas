package com.github.maracas.forges.report;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.forges.Repository;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

import java.nio.file.Path;

public record ForgeBrokenUse(
	BrokenUse brokenUse,
	String path,
	int startLine,
	int endLine,
	String url
) {
	public static ForgeBrokenUse of(BrokenUse bu, Repository repository, Path clone) {
		SourcePosition pos = bu.element().getPosition();

		// Nasty side effect work-around for pretty-printing below: we don't want to see the comments here
		bu.element().setComments(null);
		bu.usedApiElement().setComments(null);

		if (pos instanceof NoSourcePosition)
			return new ForgeBrokenUse(bu, null, -1, -1, null);

		String relativeFile = clone.toAbsolutePath().relativize(pos.getFile().toPath().toAbsolutePath()).toString();
		return new ForgeBrokenUse(
			bu,
			relativeFile,
			pos.getLine(),
			pos.getEndLine(),
			repository.buildGitHubFileUrl(repository.branch(), relativeFile, pos.getLine(), pos.getEndLine())
		);
	}
}
