package io.onedev.server.exception;

public class ObjectAlreadyExistsException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException(String message) {
		super(message);
	}

}
