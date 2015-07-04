package com.pmease.commons.git.exception;

public class ObjectAlreadyExistException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistException(String message) {
		super(message);
	}

}
