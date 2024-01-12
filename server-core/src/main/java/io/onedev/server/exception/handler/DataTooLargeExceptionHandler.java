package io.onedev.server.exception.handler;

import io.onedev.server.exception.DataTooLargeException;
import io.onedev.server.exception.HttpResponse;

import javax.servlet.http.HttpServletResponse;

public class DataTooLargeExceptionHandler extends AbstractExceptionHandler<DataTooLargeException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(DataTooLargeException exception) {
		return new HttpResponse(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, exception.getMessage());
    }
    
}
