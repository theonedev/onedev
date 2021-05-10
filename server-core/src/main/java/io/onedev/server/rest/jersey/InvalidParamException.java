package io.onedev.server.rest.jersey;

import io.onedev.commons.utils.ExplicitException;

public class InvalidParamException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public InvalidParamException(String message) {
		super(message);
	}

	public InvalidParamException(String message, Exception cause) {
		super(message, cause);
	}
	
}
