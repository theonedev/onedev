package com.turbodev.server.git.exception;

public class ObjectNotFoundException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException(String message) {
		super(message);
	}

}
