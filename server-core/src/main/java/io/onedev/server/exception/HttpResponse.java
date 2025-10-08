package io.onedev.server.exception;

import org.jspecify.annotations.Nullable;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public class HttpResponse {
	
	private final int status;
	
	private final HttpResponseBody body;
	
	private final MultivaluedMap<String, String> headers;
	
	public HttpResponse(int status, @Nullable HttpResponseBody body,
						MultivaluedMap<String, String> headers) {
		this.status = status;
		this.body = body;
		this.headers = headers;
	}
	
	public HttpResponse(int status, @Nullable HttpResponseBody body) {
		this(status, body, new MultivaluedHashMap<>());
	}

	public HttpResponse(int status) {
		this(status, (HttpResponseBody) null);
	}

	public HttpResponse(int status, String responseBodyText) {
		this(status, new HttpResponseBody(false, responseBodyText));
	}

	public HttpResponse(int status, Map<String, Object> responseBodyJsonValue) {
		this(status, new HttpResponseBody(responseBodyJsonValue));
	}
	
	public int getStatus() {
		return status;
	}

	public HttpResponseBody getBody() {
		return body;
	}

	public MultivaluedMap<String, String> getHeaders() {
		return headers;
	}
	
}
