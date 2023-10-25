package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.HttpResponseAwareException;

public class HttpResponseAwareExceptionHandler extends AbstractExceptionHandler<HttpResponseAwareException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(HttpResponseAwareException exception) {
		return exception.getHttpResponse();
    }
    
}
