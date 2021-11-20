package com.github.maracas.rest.data;

import org.kohsuke.github.GHPullRequest;

import java.nio.file.Path;
import java.util.List;

public record Delta(
	Path jarV1,
	Path jarV2,
	List<BrokenDeclaration> brokenDeclarations
) {
	public static Delta fromMaracasDelta(com.github.maracas.delta.Delta d, GHPullRequest pr, String clonePath) {
		return new Delta(
			d.getOldJar(),
			d.getNewJar(),
			d.getBrokenDeclarations()
				.stream()
				.map(decl -> BrokenDeclaration.fromMaracasDeclaration(decl, pr, clonePath))
				.toList()
		);
	}
}
