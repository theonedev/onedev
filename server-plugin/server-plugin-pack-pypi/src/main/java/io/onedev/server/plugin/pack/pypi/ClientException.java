package io.onedev.server.plugin.pack.pypi;

import io.onedev.server.exception.HttpResponseAwareException;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode, String errorMessage) {
		super(statusCode, errorMessage);
	}

	public ClientException(int statusCode) {
		super(statusCode);
	}
	
}
