package io.onedev.server.ci;

import io.onedev.server.OneException;

public class InvalidCISpecException extends OneException {

	private static final long serialVersionUID = 1L;

	public InvalidCISpecException(String message, Throwable cause) {
		super(message, cause);
	}

}
