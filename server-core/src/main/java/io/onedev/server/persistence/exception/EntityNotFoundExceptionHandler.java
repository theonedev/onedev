package io.onedev.server.persistence.exception;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

public class EntityNotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EntityNotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Database entity not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
