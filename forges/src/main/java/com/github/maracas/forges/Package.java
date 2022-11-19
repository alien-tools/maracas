package com.github.maracas.forges;

import java.nio.file.Path;

public record Package(
	String id,
	Path modulePath
) {
}
