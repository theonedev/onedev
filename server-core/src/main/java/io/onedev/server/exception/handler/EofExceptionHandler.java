package io.onedev.server.exception.handler;

import org.eclipse.jetty.io.EofException;

import io.onedev.server.exception.HttpResponse;

public class EofExceptionHandler extends AbstractExceptionHandler<EofException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EofException exception) {
		var message = exception.getMessage();
		if (message == null)
			message = "Unexpected end of stream";
		return new HttpResponse(499, message);
    }
    
}
