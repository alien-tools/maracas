package org.swat.maracas.rest.data;

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
	public static BrokenDeclaration fromMaracasDeclaration(org.swat.maracas.spoon.delta.BrokenDeclaration decl) {
		SourcePosition pos = decl.getReference().getPosition();

		String file = pos instanceof NoSourcePosition ? "" : pos.getFile().getAbsolutePath();
		int startLine = pos instanceof NoSourcePosition ? -1 : pos.getLine();
		int endLine = pos instanceof NoSourcePosition ? -1 : pos.getEndLine();

		return new BrokenDeclaration(
			SpoonHelper.fullyQualifiedName(decl.getReference()),
			decl.getChange().name(),
			file,
			startLine,
			endLine,
			null
		);
	}
}
