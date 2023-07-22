package com.github.maracas.rest.data;

import java.util.Collections;
import java.util.List;

public record ClientReport(
	String fullName,
	String url,
	List<BrokenUseDto> brokenUses,
	String error
) {
	public static ClientReport success(String fullName, String url, List<BrokenUseDto> brokenUses) {
		return new ClientReport(fullName, url, brokenUses, null);
	}

	public static ClientReport error(String fullName, String url, String error) {
		return new ClientReport(fullName, url, Collections.emptyList(), error);
	}
}
