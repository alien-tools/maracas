package org.swat.maracas.rest.data;

import java.nio.file.Path;
import java.util.List;

public record ImpactModel(
	String clientUrl,
	Path clientJar,
	Delta delta,
	List<Detection> detections,
	Throwable error
) {
	
}
