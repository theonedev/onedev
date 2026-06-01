package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;

import org.hibernate.ObjectNotFoundException;

import javax.servlet.http.HttpServletResponse;

public class ObjectNotFoundExceptionHandler extends AbstractExceptionHandler<ObjectNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ObjectNotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
