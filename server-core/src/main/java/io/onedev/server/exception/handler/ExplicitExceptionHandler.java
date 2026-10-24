package io.onedev.server.exception.handler;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.exception.HttpResponse;

import jakarta.servlet.http.HttpServletResponse;

public class ExplicitExceptionHandler extends AbstractExceptionHandler<ExplicitException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ExplicitException exception) {
		return new HttpResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
    }
    
}
