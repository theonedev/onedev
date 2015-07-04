package com.pmease.commons.git.exception;

public class ObjectNotExistException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectNotExistException(String message) {
		super(message);
	}

}
