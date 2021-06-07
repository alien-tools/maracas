package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

public class Delta {
	private List<BreakingChangeInstance> breakingChanges;
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
