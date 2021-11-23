package com.github.maracas.rest.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is infuriating, but for some reason the record version of this datatype
 * cannot be serialized _when running from tests_. No idea why. Back to the
 * ugly class version...
 */

/*
public record ClientDetections(
	String url,
	List<Detection> detections,
	Throwable error
) {
	public ClientDetections(String url, List<Detection> detections) {
		this(url, detections, null);
	}

	public ClientDetections(String url, Throwable error) {
		this(url, Collections.emptyList(), error);
	}
}
*/

public class ClientDetections {
	private String url;
	private List<Detection> detections;
	@JsonSerialize(using = ToStringSerializer.class)
	private Throwable error;

	private ClientDetections() {

	}

	public static ClientDetections empty(String u) {
		return new ClientDetections(u, Collections.emptyList(), null);
	}

	public static ClientDetections success(String u, List<Detection> d) {
		return new ClientDetections(u, d, null);
	}

	public static ClientDetections error(String u, Throwable t) {
		return new ClientDetections(u, Collections.emptyList(), t);
	}

	private ClientDetections(String u, List<Detection> d, Throwable t) {
		this.url = u;
		this.detections = d;
		this.error = t;
	}

	public String getUrl() { return url; }
	public List<Detection> getDetections() { return detections; }
	public Throwable getError() { return error; }

	public void setUrl(String u) { url = u; }
	public void setDetections(List<Detection> d) { detections = d; }
	public void setError(Throwable t) { error = t; }
}
