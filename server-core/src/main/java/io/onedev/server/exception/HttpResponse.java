package io.onedev.server.exception;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

public class HttpResponse {
	
	private final int statusCode;
	
	private final boolean jsonContent;
	
	private final String responseBody;
	
	private final MultivaluedMap<String, String> headers;
	
	public HttpResponse(int statusCode, boolean jsonContent, String responseBody, 
						MultivaluedMap<String, String> headers) {
		this.statusCode = statusCode;
		this.jsonContent = jsonContent;
		this.responseBody = responseBody;
		this.headers = headers;
	}

	public HttpResponse(int statusCode, String responseBody,
						MultivaluedMap<String, String> headers) {
		this(statusCode, false, responseBody, headers);
	}
	
	public HttpResponse(int statusCode, boolean jsonContent, String responseBody) {
		this(statusCode, jsonContent, responseBody, new MultivaluedHashMap<>());
	}

	public HttpResponse(int statusCode, String responseBody) {
		this(statusCode, false, responseBody);
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getContentType() {
		return jsonContent? APPLICATION_JSON: TEXT_PLAIN;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public MultivaluedMap<String, String> getHeaders() {
		return headers;
	}
	
}
