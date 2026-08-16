package io.onedev.server.exception.handler;

import org.eclipse.jetty.io.EofException;

import io.onedev.server.exception.HttpResponse;

public class EofExceptionHandler extends AbstractExceptionHandler<EofException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EofException exception) {
		if (exception.getMessage() != null)
			return new HttpResponse(499, exception.getMessage());
		else 
			return null;
    }
    
}
