package io.onedev.server.git.exception;

public class ObjectAlreadyExistsException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException(String message) {
		super(message);
	}

}
