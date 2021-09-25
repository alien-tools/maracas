package com.github.maracas.rest.data;

import java.util.ArrayList;
import java.util.List;

public record ClientDetections(
	String url,
	List<Detection> detections,
	Throwable error
) {
	public ClientDetections(String url, List<Detection> detections) {
		this(url, detections, null);
	}

	public ClientDetections(String url, Throwable error) {
		this(url, new ArrayList<>(), error);
	}
}
