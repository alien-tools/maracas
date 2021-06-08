package org.swat.maracas.rest.data;

import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IString;

public class BreakingChangeInstance {
	private String type;
	private String declaration;
	private String path;
	private String url;
	private int startLine;
	private int endLine;
	private boolean sourceCompatible;
	private boolean binaryCompatible;

	public BreakingChangeInstance() {

	}

	public BreakingChangeInstance(String type, String declaration, String path, int startLine, int endLine, boolean sourceCompatible, boolean binaryCompatible) {
		this.type = type;
		this.declaration = declaration;
		this.path = path;
		this.startLine = startLine;
		this.endLine = endLine;
		this.sourceCompatible = sourceCompatible;
		this.binaryCompatible = binaryCompatible;
	}

	public static BreakingChangeInstance fromRascal(IConstructor instance) {
		return new BreakingChangeInstance(
			((IString) instance.get("typ")).getValue(),
			((IString) instance.get("decl")).getValue(),
			((IString) instance.get("path")).getValue(),
			((IInteger) instance.get("startLine")).intValue(),
			((IInteger) instance.get("endLine")).intValue(),
			((IBool) instance.get("source")).getValue(),
			((IBool) instance.get("binary")).getValue()
		);
	}

	public String getType() {
		return type;
	}

	public String getDeclaration() {
		return declaration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
