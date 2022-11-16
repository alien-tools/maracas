package com.github.maracas.rest.data;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.forges.Repository;
import com.github.maracas.util.SpoonHelpers;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

import java.nio.file.Path;

public record BrokenUseDto(
	String elem,
	String used,
	String src,
	String apiUse,
	String path,
	int startLine,
	int endLine,
	String url
) {
	public static BrokenUseDto of(BrokenUse bu, Repository repository, String branch, Path clone) {
		SourcePosition pos = bu.element().getPosition();

		// Nasty side-effect work-around for pretty-printing below: we don't want to see the comments here
		bu.element().setComments(null);
		bu.usedApiElement().setComments(null);

		if (pos instanceof NoSourcePosition)
			return new BrokenUseDto(
				bu.element() instanceof CtNamedElement e ? e.getSimpleName() : bu.element().toString(),
				bu.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : bu.usedApiElement().toString(),
				SpoonHelpers.fullyQualifiedName(bu.source()),
				bu.use().name(),
				"",
				-1,
				-1,
				null
			);

		String relativeFile = clone.toAbsolutePath().relativize(pos.getFile().toPath().toAbsolutePath()).toString();
		return new BrokenUseDto(
			bu.element() instanceof CtNamedElement e ? e.getSimpleName() : bu.element().toString(),
			bu.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : bu.usedApiElement().toString(),
			SpoonHelpers.fullyQualifiedName(bu.source()),
			bu.use().name(),
			relativeFile,
			pos.getLine(),
			pos.getEndLine(),
			repository.buildGitHubFileUrl(branch, relativeFile, pos.getLine(), pos.getEndLine())
		);
	}
}
