package com.github.maracas.rest.data;

import java.util.Collections;
import java.util.List;

public record ClientReport(
	String url,
	List<BrokenUse> brokenUses,
	String error
) {
	public static ClientReport success(String u, List<BrokenUse> d) {
		return new ClientReport(u, d, null);
	}

	public static ClientReport error(String u, String e) {
		return new ClientReport(u, Collections.emptyList(), e);
	}
}
