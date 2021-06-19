package org.swat.maracas.spoon;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;

public class Detection {
	private CtElement element;
	private CtElement usedApiElement;
	private CtReference source;
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

	public CtElement getUsedApiElement() {
		return usedApiElement;
	}

	public void setUsedApiElement(CtElement usedApiElement) {
		this.usedApiElement = usedApiElement;
	}

	public CtReference getSource() {
		return source;
	}

	public void setSource(CtReference source) {
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
		String elemName =
			element instanceof CtNamedElement namedElement ?
				namedElement.getSimpleName() :
				element.toString();
		String usedName =
			usedApiElement instanceof CtNamedElement namedUsedApiElement ?
				namedUsedApiElement.getSimpleName() :
				usedApiElement.toString();
		String elemFile =
			element.getPosition() instanceof NoSourcePosition ?
				"unknown" :
				element.getPosition().getFile().getName();
		int elemLine =
			element.getPosition() instanceof NoSourcePosition ?
				-1 :
				element.getPosition().getLine();


		return """
		[%s]
			Element: %s (%s:%d)
			Used:    %s
			Source:  %s
			Use:     %s\
		""".formatted(
			change, elemName,
			elemFile, elemLine,
			usedName, source, use
		);
	}

	public String toJavaComment() {
		return "[" + change + ":" + use + "]";
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this == other)
			return true;
		if (!(other instanceof Detection))
			return false;
		Detection that = (Detection) other;
		return
			element.equals(that.getElement()) &&
			usedApiElement.equals(that.getUsedApiElement()) &&
			source.equals(that.getSource()) &&
			use.equals(that.getUse()) &&
			change.equals(that.getChange());
	}

	public enum APIUse {
		INSTANTIATION,
		METHOD_INVOCATION,
		METHOD_OVERRIDE,
		FIELD_ACCESS,
		EXTENDS,
		IMPLEMENTS,
		ANNOTATION,
		TYPE_DEPENDENCY,
		DECLARATION,
		IMPORT
	}
}
