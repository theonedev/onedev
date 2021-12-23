package io.onedev.server.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import io.onedev.server.exception.AbstractExceptionHandler;

public class WebApplicationExceptionHandler extends AbstractExceptionHandler<WebApplicationException> {

	private static final long serialVersionUID = 1L;

	@Override
	public Response getResponse(WebApplicationException throwable) {
		return throwable.getResponse();
	}

}
