package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record PackageReport(
	String id,
	String error,
	Delta delta,
	List<ClientReport> clientReports
) {
	public static PackageReport success(String id, Delta delta, List<ClientReport> clientReports) {
		return new PackageReport(id, null, delta, clientReports);
	}

	public static PackageReport error(String id, String message) {
		return new PackageReport(id, message, null, Collections.emptyList());
	}

	public Collection<BrokenUse> allBrokenUses() {
		return
			clientReports.stream()
				.map(ClientReport::brokenUses)
				.flatMap(Collection::stream)
				.toList();
	}

	public static PackageReport fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, PackageReport.class);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(json, this);
	}
}
