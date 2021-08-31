package org.swat.maracas.rest.data;

import java.nio.file.Paths;

import org.swat.maracas.spoon.SpoonHelper;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

public record Detection(
	String clientUrl,
	String elem,
	String used,
	String src,
	String apiUse,
	String path,
	int startLine,
	int endLine,
	String url
) {
	public static Detection fromMaracasDetection(org.swat.maracas.spoon.Detection d) {
		return fromMaracasDetection(d, null, null);
	}

	public static Detection fromMaracasDetection(org.swat.maracas.spoon.Detection d, String clientUrl, String clonePath) {
		SourcePosition pos = d.element().getPosition();

		if (pos instanceof NoSourcePosition)
			return new Detection(
				clientUrl,
				d.element() instanceof CtNamedElement e ? e.getSimpleName() : d.element().toString(),
				d.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : d.usedApiElement().toString(),
				SpoonHelper.fullyQualifiedName(d.source()),
				d.use().name(),
				"",
				-1,
				-1,
				null
			);

		String relativeFile = Paths.get(clonePath).relativize(pos.getFile().toPath()).toString();
		return new Detection(
			clientUrl,
			d.element() instanceof CtNamedElement e ? e.getSimpleName() : d.element().toString(),
			d.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : d.usedApiElement().toString(),
			SpoonHelper.fullyQualifiedName(d.source()),
			d.use().name(),
			relativeFile,
			pos.getLine(),
			pos.getEndLine(),
			clientUrl != null ? buildGitHubUrl(clientUrl, relativeFile, pos.getLine(), pos.getEndLine()) : null
		);
	}

	// e.g., https://github.com/tdegueul/comp-changes/blob/main/src/main/methodNoLongerStatic/MethodNoLongerStatic.java#L5-L7
	// FIXME: branches, etc.
	public static String buildGitHubUrl(String repo, String file, int beginLine, int endLine) {
		return String.format("https://github.com/%s/blob/main/%s#L%d-L%d",
			repo, file, beginLine, endLine);
	}
}
