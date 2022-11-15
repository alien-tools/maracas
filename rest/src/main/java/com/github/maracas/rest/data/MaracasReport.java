package com.github.maracas.rest.data;

import java.util.List;

public record MaracasReport(
	List<PackageReport> reports
) {
}
