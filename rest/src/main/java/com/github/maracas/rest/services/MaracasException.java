package com.github.maracas.rest.services;

public class MaracasException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MaracasException() {
    super();
	}

	public MaracasException(String message) {
	    super(message);
	}

	public MaracasException(String message, Throwable cause) {
	    super(message, cause);
	}

	public MaracasException(Throwable cause) {
	    super(cause);
	}
}
