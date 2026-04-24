package io.onedev.server.exception;

public class ServerNotFoundException extends NotFoundException {
	
	private static final long serialVersionUID = 1L;
	
	public ServerNotFoundException(String message) {
		super(message);
	}
}
