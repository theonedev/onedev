package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class HttpResponseAwareException extends ExplicitException {
	
	private final HttpResponse httpResponse;
	
	public HttpResponseAwareException(HttpResponse httpResponse) {
		super(httpResponse.getResponseBody());
		this.httpResponse = httpResponse;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
	
}
