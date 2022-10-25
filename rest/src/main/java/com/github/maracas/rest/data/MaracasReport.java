package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public record MaracasReport(
	List<PackageReport> reports
) {
	public Collection<BrokenUse> allBrokenUses() {
		return
			reports.stream()
				.map(PackageReport::allBrokenUses)
				.flatMap(Collection::stream)
				.toList();
	}

	public static MaracasReport fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());
		return objectMapper.readValue(json, MaracasReport.class);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());
		objectMapper.writeValue(json, this);
	}
}
