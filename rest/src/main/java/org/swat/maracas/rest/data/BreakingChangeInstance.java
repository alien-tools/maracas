package org.swat.maracas.rest.data;

import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IString;

public class BreakingChangeInstance {
	private final String type;
	private final String declaration;
	private final String file;
	private final int startLine;
	private final int endLine;
	private final boolean sourceCompatible;
	private final boolean binaryCompatible;

	public BreakingChangeInstance(String type, String declaration, String file, int startLine, int endLine, boolean sourceCompatible, boolean binaryCompatible) {
		this.type = type;
		this.declaration = declaration;
		this.file = file;
		this.startLine = startLine;
		this.endLine = endLine;
		this.sourceCompatible = sourceCompatible;
		this.binaryCompatible = binaryCompatible;
	}

	public static BreakingChangeInstance fromRascal(IConstructor instance) {
		return new BreakingChangeInstance(
			((IString) instance.get("typ")).getValue(),
			((IString) instance.get("decl")).getValue(),
			((IString) instance.get("file")).getValue(),
			((IInteger) instance.get("startLine")).intValue(),
			((IInteger) instance.get("endLine")).intValue(),
			((IBool) instance.get("source")).getValue(),
			((IBool) instance.get("binary")).getValue()
		);
	}

	public String getDeclaration() {
		return declaration;
	}

	public String getType() {
		return type;
	}

	public String getFile() {
		return file;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public boolean getSourceCompatible() {
		return sourceCompatible;
	}

	public boolean getBinaryCompatible() {
		return binaryCompatible;
	}
}
