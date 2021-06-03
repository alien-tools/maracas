package org.swat.maracas.rest.data;

public class BreakingChangeInstance {
	private final String declaration;
	private final String name;

	public BreakingChangeInstance(String declaration, String name) {
		this.declaration = declaration;
		this.name = name;
	}
	
	public String getDeclaration() {
		return declaration;
	}

	public String getName() {
		return name;
	}
}
