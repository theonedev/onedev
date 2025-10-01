package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

public class ValidationExceptionHandler extends AbstractExceptionHandler<ValidationException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ValidationException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Validation error";
		return new HttpResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, errorMessage);
    }
    
}
