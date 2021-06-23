package org.swat.maracas.rest.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

public class ImpactModel {
	private String clientUrl;
	private Path clientJar;
	private Delta delta;
	private List<Detection> detections = new ArrayList<>();
	private Throwable error;

	public ImpactModel() {

	}

	public ImpactModel(List<Detection> detections) {
		this.detections = detections;
	}

	public ImpactModel(Throwable error) {
		this.error = error;
	}

	public String getClientUrl() {
		return clientUrl;
	}

	public void setClientUrl(String clientUrl) {
		this.clientUrl = clientUrl;
	}

	public Path getClientJar() {
		return clientJar;
	}

	public void setClientJar(Path clientJar) {
		this.clientJar = clientJar;
	}

	public Delta getDelta() {
		return delta;
	}

	public void setDelta(Delta delta) {
		this.delta = delta;
	}

	public List<Detection> getDetections() {
		return detections;
	}

	public void addDetection(Detection d) {
		detections.add(d);
	}

	public Throwable getError() {
		return error;
	}

	public static ImpactModel fromRascal(IList detections) {
		return new ImpactModel(
			detections.stream()
				.map(d -> Detection.fromRascal((IConstructor) d))
				.collect(Collectors.toList())
		);
	}
}
