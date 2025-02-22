package io.onedev.server.exception;

import java.util.Map;

import org.eclipse.jetty.http.HttpStatus;

import io.onedev.commons.utils.ExplicitException;

public class HttpResponseAwareException extends ExplicitException {
	
	private final HttpResponse httpResponse;
	
	public HttpResponseAwareException(HttpResponse httpResponse) {
		super(httpResponse.getBody() != null ? httpResponse.getBody().getText(): HttpStatus.getMessage(httpResponse.getStatus()));
		this.httpResponse = httpResponse;
	}

	public HttpResponseAwareException(int status, String responseBodyText) {
		this(new HttpResponse(status, responseBodyText));
	}
	
	public HttpResponseAwareException(int status) {
		this(new HttpResponse(status, (HttpResponseBody)null));
	}
	
	public HttpResponseAwareException(int status, Map<String, Object> responseBodyJsonValue) {
		this(new HttpResponse(status, responseBodyJsonValue));
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
	
}
