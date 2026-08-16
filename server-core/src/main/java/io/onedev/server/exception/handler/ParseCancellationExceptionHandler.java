package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import io.onedev.server.exception.HttpResponse;

public class ParseCancellationExceptionHandler extends AbstractExceptionHandler<ParseCancellationException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ParseCancellationException exception) {
		return new HttpResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, "Invalid query");
    }
    
}
