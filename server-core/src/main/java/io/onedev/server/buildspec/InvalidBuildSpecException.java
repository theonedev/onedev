package io.onedev.server.buildspec;

import io.onedev.server.OneException;

public class InvalidBuildSpecException extends OneException {

	private static final long serialVersionUID = 1L;

	public InvalidBuildSpecException(String message, Throwable cause) {
		super(message, cause);
	}

}
