package com.pmease.commons.git;

public class ObjectNotFoundException extends GitException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException(String message) {
		super(message);
	}
	
}
