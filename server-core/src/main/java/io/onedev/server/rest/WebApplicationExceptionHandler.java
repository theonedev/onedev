package io.onedev.server.rest;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.HttpResponseBody;
import io.onedev.server.exception.handler.AbstractExceptionHandler;
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.WebApplicationException;

public class WebApplicationExceptionHandler extends AbstractExceptionHandler<WebApplicationException> {

	private static final long serialVersionUID = 1L;

	@Override
	public HttpResponse getResponse(WebApplicationException throwable) {
		var jerseyResponse = throwable.getResponse();
		var message = throwable.getMessage();
		if (message == null)
			message = HttpStatus.getMessage(jerseyResponse.getStatus());
		return new HttpResponse(jerseyResponse.getStatus(), 
				new HttpResponseBody(false, message), 
				jerseyResponse.getStringHeaders());
	}

}
