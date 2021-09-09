package org.swat.maracas.rest.data;

import java.nio.file.Paths;

import org.swat.maracas.spoon.SpoonHelper;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;

public record BrokenDeclaration(
	String declaration,
	String change,
	String path,
	int startLine,
	int endLine,
	String url
) {
	public static BrokenDeclaration fromMaracasDeclaration(org.swat.maracas.spoon.delta.BrokenDeclaration decl, String repository, String clonePath) {
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

		String relativeFile = Paths.get(clonePath).relativize(Paths.get(file)).toString();
		return new BrokenDeclaration(
			SpoonHelper.fullyQualifiedName(decl.getReference()),
			decl.getChange().name(),
			relativeFile,
			startLine,
			endLine,
			repository != null ? GitHubUtils.buildGitHubUrl(repository, relativeFile, startLine, endLine) : null
		);
	}
}
