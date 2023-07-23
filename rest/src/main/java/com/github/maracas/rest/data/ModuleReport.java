package com.github.maracas.rest.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record ModuleReport(
	String id,
	String error,
	DeltaDto delta,
	List<ClientReport> clientReports
) {
	public static ModuleReport success(String id, DeltaDto delta, List<ClientReport> clientReports) {
		return new ModuleReport(id, null, delta, clientReports);
	}

	public static ModuleReport error(String id, String message) {
		return new ModuleReport(id, message, null, Collections.emptyList());
	}

	public Collection<BrokenUseDto> allBrokenUses() {
		return
			clientReports.stream()
				.map(ClientReport::brokenUses)
				.flatMap(Collection::stream)
				.toList();
	}
}
