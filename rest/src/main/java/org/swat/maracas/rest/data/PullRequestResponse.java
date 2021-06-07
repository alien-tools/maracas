package org.swat.maracas.rest.data;

public class PullRequestResponse {
	private final String message;
	private final Delta delta;

	public PullRequestResponse(String message, Delta delta) {
		this.message = message;
		this.delta = delta;
	}

	public String getMessage() {
		return message;
	}

	public Delta getDelta() {
		return delta;
	}
}
