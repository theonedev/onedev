package io.onedev.server.exception.handler;

import java.io.Serial;

import org.eclipse.jetty.io.EofException;

import io.onedev.server.exception.HttpResponse;

public class EofExceptionHandler extends AbstractExceptionHandler<EofException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EofException exception) {
		var message = exception.getMessage();
		if (message == null)
			message = "Unexpected end of stream";
		return new HttpResponse(499, message);
    }
    
}
