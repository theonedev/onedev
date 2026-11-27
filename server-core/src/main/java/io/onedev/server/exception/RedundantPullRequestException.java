package io.onedev.server.exception;

public class RedundantPullRequestException extends NotAcceptableException {

	public RedundantPullRequestException(String message) {
		super(message);
	}

}
