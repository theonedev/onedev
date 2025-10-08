package io.onedev.server.plugin.pack.npm;

import io.onedev.server.exception.HttpResponseAwareException;

import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode, @Nullable String errorMessage) {
		super(statusCode, getJsonValue(errorMessage));
	}

	public ClientException(int statusCode) {
		this(statusCode, null);
	}
	
	private static Map<String, Object> getJsonValue(@Nullable String errorMessage) {
		Map<String, Object> value = new HashMap<>();
		value.put("error", errorMessage!=null? errorMessage: "");
		return value;
	}
}
