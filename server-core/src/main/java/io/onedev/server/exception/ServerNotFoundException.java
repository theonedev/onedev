package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class ServerNotFoundException extends ExplicitException {
	
	private static final long serialVersionUID = 1L;
	
	public ServerNotFoundException(String message) {
		super(message);
	}
}
