package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public record PackageReport(
	String id,
	Delta delta,
	List<ClientReport> clientReports
) {
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
