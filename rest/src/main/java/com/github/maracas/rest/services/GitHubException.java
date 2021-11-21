package com.github.maracas.rest.services;

import java.io.Serial;

public class GitHubException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public GitHubException(String message, Throwable cause) {
		super(message, cause);
	}
}
