package io.onedev.server.exception.handler;

import java.io.Serial;

import jakarta.servlet.http.HttpServletResponse;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.NotAcceptableException;

public class NotAcceptableExceptionHandler extends AbstractExceptionHandler<NotAcceptableException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(NotAcceptableException exception) {
		return new HttpResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, exception.getMessage());
    }
    
}
