package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.errors.MissingObjectException;

import io.onedev.server.exception.HttpResponse;

public class MissingObjectExceptionHandler extends AbstractExceptionHandler<MissingObjectException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(MissingObjectException exception) {
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
    }
    
}
