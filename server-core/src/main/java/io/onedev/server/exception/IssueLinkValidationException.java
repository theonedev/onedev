package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class IssueLinkValidationException extends ExplicitException {

	public IssueLinkValidationException(String message) {
		super(message);
	}

}