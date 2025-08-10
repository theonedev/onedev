package io.onedev.server.rest;

import javax.ws.rs.BadRequestException;

public class InvalidParamsException extends BadRequestException {

	private static final long serialVersionUID = 1L;

	public InvalidParamsException(String message) {
		super(message);
	}

	public InvalidParamsException(String message, Exception cause) {
		super(message, cause);
	}
	
}
