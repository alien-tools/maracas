package org.swat.maracas.rest.data;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IString;

public class Detection {
	private String client;
	private String elem;
	private String used;
	private String src;
	private String apiUse;

	public Detection() {
		
	}

	public Detection(String elem, String used, String src, String apiUse) {
		this.elem = elem;
		this.used = used;
		this.src = src;
		this.apiUse = apiUse;
	}

	public Detection(String client, String elem, String used, String src, String apiUse) {
		this(elem, used, src, apiUse);
		this.client = client;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
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

	public static Detection fromRascal(IConstructor detection) {
		return new Detection(
			((IString) detection.get("elem")).getValue(),
			((IString) detection.get("used")).getValue(),
			((IString) detection.get("src")).getValue(),
			((IString) detection.get("apiUse")).getValue()
		);
	}
}
