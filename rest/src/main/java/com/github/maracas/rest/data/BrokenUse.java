package com.github.maracas.rest.data;

import com.github.maracas.rest.util.GitHubUtils;
import com.github.maracas.util.SpoonHelpers;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

import java.nio.file.Paths;

public record BrokenUse(
	String elem,
	String used,
	String src,
	String apiUse,
	String path,
	int startLine,
	int endLine,
	String url
) {
	public static BrokenUse fromMaracasBrokenUse(com.github.maracas.brokenUse.BrokenUse d, String owner, String repository, String ref, String clonePath) {
		SourcePosition pos = d.element().getPosition();

		if (pos instanceof NoSourcePosition)
			return new BrokenUse(
				d.element() instanceof CtNamedElement e ? e.getSimpleName() : d.element().toString(),
				d.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : d.usedApiElement().toString(),
				SpoonHelpers.fullyQualifiedName(d.source()),
				d.use().name(),
				"",
				-1,
				-1,
				null
			);

		String relativeFile = Paths.get(clonePath).toAbsolutePath().relativize(pos.getFile().toPath().toAbsolutePath()).toString();
		return new BrokenUse(
			d.element() instanceof CtNamedElement e ? e.getSimpleName() : d.element().toString(),
			d.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : d.usedApiElement().toString(),
			SpoonHelpers.fullyQualifiedName(d.source()),
			d.use().name(),
			relativeFile,
			pos.getLine(),
			pos.getEndLine(),
			GitHubUtils.buildGitHubFileUrl(owner, repository, ref, relativeFile, pos.getLine(), pos.getEndLine())
		);
	}
}
