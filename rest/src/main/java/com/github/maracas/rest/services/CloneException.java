package com.github.maracas.rest.services;

import java.io.Serial;

public class CloneException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public CloneException(Throwable cause) {
		super(cause);
	}
}
