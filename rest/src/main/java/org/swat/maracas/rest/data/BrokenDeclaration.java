package org.swat.maracas.rest.data;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

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

		String fqn = "";
		if (decl.getReference() instanceof CtTypeReference<?> tRef)
			fqn = tRef.getQualifiedName();
		else if (decl.getReference() instanceof CtExecutableReference<?> eRef)
			fqn = eRef.getDeclaringType().getQualifiedName().concat(".").concat(eRef.getSimpleName());
		else if (decl.getReference() instanceof CtFieldReference<?> fRef)
			fqn = fRef.getDeclaringType().getQualifiedName().concat(".").concat(fRef.getSimpleName());

		return new BrokenDeclaration(
			fqn,
			decl.getChange().name(),
			file,
			startLine,
			endLine,
			null
		);
	}
}
