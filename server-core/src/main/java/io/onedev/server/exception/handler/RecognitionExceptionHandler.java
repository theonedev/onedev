package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.antlr.v4.runtime.RecognitionException;

import io.onedev.server.exception.HttpResponse;

public class RecognitionExceptionHandler extends AbstractExceptionHandler<RecognitionException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(RecognitionException exception) {
		return new HttpResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, "Invalid query");
    }
    
}
