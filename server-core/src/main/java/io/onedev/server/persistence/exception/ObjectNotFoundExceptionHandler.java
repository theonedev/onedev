package io.onedev.server.persistence.exception;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;
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
