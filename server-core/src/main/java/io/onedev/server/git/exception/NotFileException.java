package io.onedev.server.git.exception;

import io.onedev.commons.utils.ExplicitException;

public class NotFileException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public NotFileException(String message) {
		super(message);
	}
	
}
