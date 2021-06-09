package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

public class Delta {
	private Path jarV1;
	private Path jarV2;
	private Path sources;
	private List<BreakingChangeInstance> breakingChanges = new ArrayList<>();
	private Throwable error;

	public Delta() {

	}

	public Delta(List<BreakingChangeInstance> breakingChanges, Throwable error) {
		this.breakingChanges = breakingChanges;
		this.error = error;

	}

	public Delta(List<BreakingChangeInstance> breakingChanges) {
		this(breakingChanges, null);
	}

	public void toJson(File json) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(json, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Path getJarV1() {
		return jarV1;
	}

	public Path getJarV2() {
		return jarV2;
	}

	public Path getSources() {
		return sources;
	}

	public void setJarV1(Path jarV1) {
		this.jarV1 = jarV1;
	}

	public void setJarV2(Path jarV2) {
		this.jarV2 = jarV2;
	}

	public void setSources(Path sources) {
		this.sources = sources;
	}

	public List<BreakingChangeInstance> getBreakingChanges() {
		return breakingChanges;
	}

	public Throwable getError() {
		return error;
	}

	public static Delta fromRascal(IList delta) {
		return new Delta(
			delta.stream()
				.map(e -> BreakingChangeInstance.fromRascal((IConstructor) e))
				.collect(Collectors.toList())
		);
	}

	public static Delta fromJson(File json) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(json, Delta.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
