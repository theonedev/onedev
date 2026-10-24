package io.onedev.server.git.exception;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;

public class NotTreeException extends ExplicitException {

	@Serial
	private static final long serialVersionUID = 1L;

	public NotTreeException(String message) {
		super(message);
	}
	
}
