package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;
import org.eclipse.jetty.http.HttpStatus;

import javax.annotation.Nullable;
import java.util.Map;

public class HttpResponseAwareException extends ExplicitException {
	
	private final HttpResponse httpResponse;
	
	public HttpResponseAwareException(HttpResponse httpResponse) {
		super(httpResponse.getBody() != null ? httpResponse.getBody().getText(): HttpStatus.getMessage(httpResponse.getStatus()));
		this.httpResponse = httpResponse;
	}

	public HttpResponseAwareException(int status, @Nullable String responseBodyText) {
		this(new HttpResponse(status, responseBodyText));
	}
	
	public HttpResponseAwareException(int status) {
		this(status, (String)null);
	}
	
	public HttpResponseAwareException(int status, Map<String, Object> responseBodyJsonValue) {
		this(new HttpResponse(status, responseBodyJsonValue));
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
	
}
