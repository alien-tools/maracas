package org.swat.maracas.rest.tasks;

public class BuildException extends RuntimeException {
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
