package io.onedev.server.web.exceptionhandler;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;
import org.apache.wicket.core.request.mapper.StalePageException;

import javax.servlet.http.HttpServletResponse;

public class StalePageExceptionHandler extends AbstractExceptionHandler<StalePageException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(StalePageException exception) {
		String errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Stale page";
		return new HttpResponse(HttpServletResponse.SC_REQUEST_TIMEOUT, errorMessage);
    }
    
}
