package com.github.maracas.rest.data;

import com.github.maracas.forges.report.ForgeBrokenUse;
import com.github.maracas.util.SpoonHelpers;
import spoon.reflect.declaration.CtNamedElement;

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
	public static BrokenUseDto of(ForgeBrokenUse bu) {
		return new BrokenUseDto(
			bu.brokenUse().element() instanceof CtNamedElement e ? e.getSimpleName() : bu.brokenUse().element().toString(),
			bu.brokenUse().usedApiElement() instanceof CtNamedElement e ? e.getSimpleName() : bu.brokenUse().usedApiElement().toString(),
			SpoonHelpers.fullyQualifiedName(bu.brokenUse().source()),
			bu.brokenUse().use().name(),
			bu.path(),
			bu.startLine(),
			bu.endLine(),
			bu.url()
		);
	}
}
