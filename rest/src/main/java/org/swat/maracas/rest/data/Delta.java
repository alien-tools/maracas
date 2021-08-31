package org.swat.maracas.rest.data;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public record Delta(
	Path jarV1,
	Path jarV2,
	List<BrokenDeclaration> brokenDeclarations
) {
	public static Delta fromMaracasDelta(org.swat.maracas.spoon.delta.Delta d, String repository, String clonePath) {
		return new Delta(
			d.getV1(),
			d.getV2(),
			d.getBrokenDeclarations()
				.stream()
				.map(decl -> BrokenDeclaration.fromMaracasDeclaration(decl, repository, clonePath))
				.collect(Collectors.toList())
		);
	}
}
