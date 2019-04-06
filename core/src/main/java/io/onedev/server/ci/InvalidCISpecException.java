package io.onedev.server.ci;

import io.onedev.server.exception.OneException;

public class InvalidCISpecException extends OneException {

	private static final long serialVersionUID = 1L;

	public InvalidCISpecException(String message, Exception cause) {
		super(message, cause);
	}

}
