package io.onedev.server.web.exceptionhandler;

import java.io.Serial;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;
import org.apache.wicket.core.request.mapper.StalePageException;

import jakarta.servlet.http.HttpServletResponse;

public class StalePageExceptionHandler extends AbstractExceptionHandler<StalePageException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(StalePageException exception) {
		String errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Stale page";
		return new HttpResponse(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
    }
    
}
