package io.onedev.server.git.exception;

import io.onedev.server.exception.OneException;

public class GitException extends OneException {

	private static final long serialVersionUID = 1L;

	public GitException(String message) {
		super(message);
	}
	
	public GitException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitException(Throwable cause) {
		super(cause);
	}
	
}

