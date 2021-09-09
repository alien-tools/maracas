package org.swat.maracas.rest.data;

import java.util.List;

public record ClientDetections(
	String url,
	List<Detection> detections
) {

}
