package org.swat.maracas.spoon;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtReference;

public class Detection {
	private CtElement element;
	private CtReference reference;
	private CtElement usedApiElement;
	private String source;
	private APIUse use;
	private JApiCompatibilityChange change;

	public Detection() {
		
	}

	public void setElement(CtElement element) {
		this.element = element;
	}

	public void setReference(CtReference reference) {
		this.reference = reference;
	}

	public void setUsedApiElement(CtElement usedApiElement) {
		this.usedApiElement = usedApiElement;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setUse(APIUse use) {
		this.use = use;
	}

	public void setChange(JApiCompatibilityChange change) {
		this.change = change;
	}

	@Override
	public String toString() {
		if (reference.getPosition() instanceof NoSourcePosition) {
			System.out.println("elem="+element+" ["+element.getPosition()+"]");
			System.out.println("parent="+element.getParent()+" ["+element.getParent().getPosition()+"]");
			System.out.println("ref="+reference+" ["+reference.isImplicit()+"]");
			System.out.println(usedApiElement.isImplicit());
			System.out.println(use);
		}
		String[] elemLines = element.toString().split("\\n");
		String[] usedLines = usedApiElement.toString().split("\\n");
		return String.format("[%s] %s (%s:%d) uses (%s -> %s) [%s]",
			change, elemLines[0], element.getPosition().getFile().getName(), element.getPosition().getLine(), usedLines[0], source, use);
	}

	enum APIUse {
		METHOD_INVOCATION,
		METHOD_OVERRIDE,
		FIELD_ACCESS,
		EXTENDS,
		IMPLEMENTS,
		ANNOTATION,
		TYPE_DEPENDENCY,
		DECLARATION
	}
}
