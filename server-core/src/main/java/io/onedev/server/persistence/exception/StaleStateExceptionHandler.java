package io.onedev.server.persistence.exception;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;
import org.hibernate.StaleStateException;

import javax.servlet.http.HttpServletResponse;

public class StaleStateExceptionHandler extends AbstractExceptionHandler<StaleStateException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(StaleStateException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Persistence state staled";
		return new HttpResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
    }
    
}
