package io.onedev.server.rest;

import javax.ws.rs.BadRequestException;

public class InvalidParamException extends BadRequestException {

	private static final long serialVersionUID = 1L;

	public InvalidParamException(String message) {
		super(message);
	}

	public InvalidParamException(String message, Exception cause) {
		super(message, cause);
	}
	
}
