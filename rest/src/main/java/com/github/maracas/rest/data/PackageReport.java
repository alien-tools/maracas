package com.github.maracas.rest.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record PackageReport(
	String id,
	String error,
	DeltaDto delta,
	List<ClientReport> clientReports
) {
	public static PackageReport success(String id, DeltaDto delta, List<ClientReport> clientReports) {
		return new PackageReport(id, null, delta, clientReports);
	}

	public static PackageReport error(String id, String message) {
		return new PackageReport(id, message, null, Collections.emptyList());
	}

	public Collection<BrokenUseDto> allBrokenUses() {
		return
			clientReports.stream()
				.map(ClientReport::brokenUses)
				.flatMap(Collection::stream)
				.toList();
	}
}
