package io.onedev.server.buildspec;

import io.onedev.commons.utils.ExplicitException;

public class InvalidBuildSpecException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public InvalidBuildSpecException(String message, Throwable cause) {
		super(message, cause);
	}

}
