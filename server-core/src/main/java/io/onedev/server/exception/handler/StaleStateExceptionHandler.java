package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.StaleStateException;

import io.onedev.server.exception.HttpResponse;

public class StaleStateExceptionHandler extends AbstractExceptionHandler<StaleStateException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(StaleStateException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Persistence state staled";
		return new HttpResponse(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
    }
    
}
