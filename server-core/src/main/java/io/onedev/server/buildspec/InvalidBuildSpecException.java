package io.onedev.server.buildspec;

import io.onedev.server.GeneralException;

public class InvalidBuildSpecException extends GeneralException {

	private static final long serialVersionUID = 1L;

	public InvalidBuildSpecException(String message, Throwable cause) {
		super(message, cause);
	}

}
