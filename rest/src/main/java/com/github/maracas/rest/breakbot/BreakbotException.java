package com.github.maracas.rest.breakbot;

import java.io.Serial;

public class BreakbotException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public BreakbotException(String message) {
		super(message);
	}

	public BreakbotException(String message, Throwable cause) {
		super(message, cause);
	}

	public BreakbotException(Throwable cause) {
		super(cause);
	}
}
