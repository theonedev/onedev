package io.onedev.server.exception;

import java.io.Serializable;

import javax.ws.rs.core.Response;

public interface ExceptionHandler<T extends Throwable> extends Serializable {
	
	Response getResponse(T throwable);
	
	Class<T> getExceptionClass();

	Response getResponseWith(Throwable throwable);
	
}
