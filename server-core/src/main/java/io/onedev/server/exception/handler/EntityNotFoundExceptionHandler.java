package io.onedev.server.exception.handler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import io.onedev.server.exception.HttpResponse;

public class EntityNotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EntityNotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
