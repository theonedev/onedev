package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.NotFoundException;

public class NotFoundExceptionHandler extends AbstractExceptionHandler<NotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(NotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
