package org.swat.maracas.rest.data;

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
		SourcePosition pos = d.element().getPosition();

		return new Detection(
			d.element() instanceof CtNamedElement e ? e.getSimpleName() : d.element().toString(),
			d.usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : d.usedApiElement().toString(),
			SpoonHelper.fullyQualifiedName(d.source()),
			d.use().name(),
			pos instanceof NoSourcePosition ? "" : pos.getFile().getAbsolutePath(),
			pos instanceof NoSourcePosition ? -1 : pos.getLine(),
			pos instanceof NoSourcePosition ? -1 : pos.getEndLine()
		);
	}

	public Detection(String elem, String used, String src, String apiUse) {
		this(null, elem, used, src, apiUse, null, -1, -1, null);
	}

	public Detection(String elem, String used, String src, String apiUse, String path, int startLine, int endLine) {
		this(null, elem, used, src, apiUse, path, startLine, endLine, null);
	}

}
