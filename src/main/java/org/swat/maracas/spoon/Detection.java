package org.swat.maracas.spoon;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
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

	public CtElement getElement() {
		return element;
	}

	public void setElement(CtElement element) {
		this.element = element;
	}

	public CtReference getReference() {
		return reference;
	}

	public void setReference(CtReference reference) {
		this.reference = reference;
	}

	public CtElement getUsedApiElement() {
		return usedApiElement;
	}

	public void setUsedApiElement(CtElement usedApiElement) {
		this.usedApiElement = usedApiElement;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public APIUse getUse() {
		return use;
	}

	public void setUse(APIUse use) {
		this.use = use;
	}

	public JApiCompatibilityChange getChange() {
		return change;
	}

	public void setChange(JApiCompatibilityChange change) {
		this.change = change;
	}

	@Override
	public String toString() {
		if (reference.getPosition().getFile() == null) {
//			System.out.println("elem="+element+" ["+element.getPosition()+"]");
//			System.out.println("parent="+element.getParent()+" ["+element.getParent().getPosition()+"]");
//			System.out.println("ref="+reference+" ["+reference.isImplicit()+"]");
//			System.out.println(usedApiElement.isImplicit());
//			System.out.println(use);
			System.out.println("Unknown reference " + reference);
		}
		if (element.getPosition().getFile() == null) {
			System.out.println("Unknown element " + element);
		}
		if (element.getPosition() instanceof NoSourcePosition) {
			System.out.println("No source position for " + element);
		}
//		String[] elemLines = element.toString().split("\\n");
//		String[] usedLines = usedApiElement.toString().split("\\n");
		String elemName = ((CtNamedElement) element).getSimpleName();
		String usedName = ((CtNamedElement) usedApiElement).getSimpleName();
		return String.format("[%s] %s (%s:%d) (%d:%d) uses (%s -> %s) [%s]",
			change, elemName, element.getPosition().getFile().getName(), element.getPosition().getLine(),
			element.getPosition().getColumn(), element.getPosition().getEndColumn(), usedName, source, use);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this == other)
			return true;
		Detection that = (Detection) other;
		return  element.equals(that.getElement()) &&
				reference.equals(that.getReference()) &&
				usedApiElement.equals(that.getUsedApiElement()) &&
				source.equals(that.getSource()) &&
				use.equals(that.getUse()) &&
				change.equals(that.getChange());
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
