package io.onedev.server.exception;

import java.io.Serial;

public class ServerNotFoundException extends NotFoundException {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	public ServerNotFoundException(String message) {
		super(message);
	}
}
