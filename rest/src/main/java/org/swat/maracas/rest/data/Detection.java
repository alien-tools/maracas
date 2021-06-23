package org.swat.maracas.rest.data;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IString;

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

	public Detection() {

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

	public static Detection fromRascal(IConstructor detection) {
		return new Detection(
			((IString) detection.get("elem")).getValue(),
			((IString) detection.get("used")).getValue(),
			((IString) detection.get("src")).getValue(),
			((IString) detection.get("apiUse")).getValue(),
			((IString) detection.get("path")).getValue(),
			((IInteger) detection.get("startLine")).intValue(),
			((IInteger) detection.get("endLine")).intValue()
		);
	}
}
