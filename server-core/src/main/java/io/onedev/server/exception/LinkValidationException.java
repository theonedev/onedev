package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class LinkValidationException extends ExplicitException {

	public LinkValidationException(String message) {
		super(message);
	}

}