package io.onedev.server.git.exception;

import java.io.Serial;

import io.onedev.server.exception.HttpResponseAwareException;

import jakarta.servlet.http.HttpServletResponse;

public class ObjectAlreadyExistsException extends HttpResponseAwareException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException(String message) {
		super(HttpServletResponse.SC_CONFLICT, message);
	}

}
