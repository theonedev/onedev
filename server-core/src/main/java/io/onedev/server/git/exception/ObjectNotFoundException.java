package io.onedev.server.git.exception;

import io.onedev.server.exception.HttpResponseAwareException;

import javax.servlet.http.HttpServletResponse;

public class ObjectNotFoundException extends HttpResponseAwareException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException(String message) {
		super(HttpServletResponse.SC_NOT_FOUND, message);
	}

}
