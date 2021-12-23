package io.onedev.server.git.exception;

import io.onedev.commons.utils.ExplicitException;

public class GitException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public GitException(String message) {
		super(message);
	}
	
	public GitException(String message, Throwable cause) {
		super(message, cause);
	}

}

