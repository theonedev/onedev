package io.onedev.server.git.exception;

import io.onedev.server.exception.HttpResponseAwareException;

import javax.servlet.http.HttpServletResponse;

public class ObjectAlreadyExistsException extends HttpResponseAwareException {

	private static final long serialVersionUID = 1L;

	public ObjectAlreadyExistsException(String message) {
		super(HttpServletResponse.SC_CONFLICT, message);
	}

}
