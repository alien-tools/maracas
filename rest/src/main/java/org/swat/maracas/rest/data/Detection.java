package org.swat.maracas.rest.data;

import org.swat.maracas.spoon.SpoonHelper;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

public class Detection {
	private String clientUrl;
	private String elem;
	private String used;
	private String src;
	private String apiUse;
	private String path;
	private int startLine;
	private int endLine;
	private String url;

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
		this.elem = elem;
		this.used = used;
		this.src = src;
		this.apiUse = apiUse;
	}

	public Detection(String elem, String used, String src, String apiUse, String path, int startLine, int endLine) {
		this(elem, used, src, apiUse);
		this.path = path;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public String getClientUrl() {
		return clientUrl;
	}

	public void setClientUrl(String clientUrl) {
		this.clientUrl = clientUrl;
	}

	public String getElem() {
		return elem;
	}

	public void setElem(String elem) {
		this.elem = elem;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getApiUse() {
		return apiUse;
	}

	public void setApiUse(String apiUse) {
		this.apiUse = apiUse;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
