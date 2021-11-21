package com.github.maracas.rest.services;

import java.io.Serial;

public class BuildException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public BuildException(String message) {
		super(message);
	}

	public BuildException(String message, Throwable cause) {
		super(message, cause);
	}

	public BuildException(Throwable cause) {
		super(cause);
	}
}
