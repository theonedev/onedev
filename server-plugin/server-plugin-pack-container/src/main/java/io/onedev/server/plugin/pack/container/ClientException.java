package io.onedev.server.plugin.pack.container;

import io.onedev.server.exception.HttpResponseAwareException;

import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode, ErrorCode errorCode, @Nullable String errorMessage) {
		super(statusCode, toJsonValue(errorCode, errorMessage));
	}
	
	public ClientException(int statusCode, ErrorCode errorCode) {
		this(statusCode, errorCode, null);
	}
	
	public static Map<String, Object> toJsonValue(ErrorCode errorCode, @Nullable String errorMessage) {
		var value = new HashMap<String, Object>();
		var error = new HashMap<String, Object>();
		error.put("code", errorCode.name());
		if (errorMessage != null)
			error.put("message", errorMessage);
		var errors = new Object[] {error};
		value.put("errors", errors);
		return value;
	}
	
}
