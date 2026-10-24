package io.onedev.server.exception.handler;

import java.io.Serial;

import jakarta.servlet.http.HttpServletResponse;

import io.onedev.server.exception.BadRequestException;
import io.onedev.server.exception.HttpResponse;

public class BadRequestExceptionHandler extends AbstractExceptionHandler<BadRequestException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(BadRequestException exception) {
		return new HttpResponse(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
    }
    
}
