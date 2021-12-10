package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Collections;
import java.util.List;

public record ClientReport(
	String url,
	List<BrokenUse> brokenUses,
	@JsonSerialize(using = ToStringSerializer.class)
	Throwable error
) {
	public static ClientReport empty(String u) {
		return new ClientReport(u, Collections.emptyList(), null);
	}

	public static ClientReport success(String u, List<BrokenUse> d) {
		return new ClientReport(u, d, null);
	}

	public static ClientReport error(String u, Throwable t) {
		return new ClientReport(u, Collections.emptyList(), t);
	}
}
