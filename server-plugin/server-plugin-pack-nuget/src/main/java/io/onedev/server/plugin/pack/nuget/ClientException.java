package io.onedev.server.plugin.pack.nuget;

import io.onedev.server.exception.HttpResponseAwareException;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode) {
		super(statusCode);
	}
	
}
