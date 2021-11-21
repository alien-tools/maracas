package com.github.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record MaracasReport(
	Delta delta,
	List<ClientDetections> clientDetections,
	@JsonSerialize(using = ToStringSerializer.class)
	Throwable error
) {
	public MaracasReport(Delta delta, List<ClientDetections> clientDetections) {
		this(delta, clientDetections, null);
	}

	public MaracasReport(Throwable error) {
		this(null, null, error);
	}

	public static MaracasReport fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, MaracasReport.class);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(json, this);
	}
}
