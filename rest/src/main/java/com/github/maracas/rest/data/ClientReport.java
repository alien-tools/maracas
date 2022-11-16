package com.github.maracas.rest.data;

import java.util.Collections;
import java.util.List;

public record ClientReport(
	String url,
	List<BrokenUseDto> brokenUses,
	String error
) {
	public static ClientReport success(String url, List<BrokenUseDto> brokenUses) {
		return new ClientReport(url, brokenUses, null);
	}

	public static ClientReport error(String url, String error) {
		return new ClientReport(url, Collections.emptyList(), error);
	}
}
