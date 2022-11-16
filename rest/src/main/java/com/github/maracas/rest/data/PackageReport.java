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

	public Collection<ClientReport> brokenClients() {
		return
			clientReports.stream()
				.filter(r -> !r.brokenUses().isEmpty())
				.toList();
	}

	public Collection<ClientReport> unimpactedClients() {
		return
			clientReports.stream()
				.filter(r -> r.brokenUses().isEmpty())
				.toList();
	}

	public Collection<BreakingChangeDto> impactfulBreakingChanges() {
		return
			delta.breakingChanges().stream()
				.filter(bc -> allBrokenUses().stream().anyMatch(bu -> bu.src().equals(bc.declaration())))
				.toList();
	}
}
