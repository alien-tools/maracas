package com.github.maracas.rest.services;

import java.io.Serial;

public class MaracasException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public MaracasException(String message, Throwable cause) {
	    super(message, cause);
	}
}
