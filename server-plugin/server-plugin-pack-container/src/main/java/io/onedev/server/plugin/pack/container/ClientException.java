package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;
import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.HttpResponseAwareException;

import java.util.HashMap;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode, ErrorCode errorCode, String errorMessage) {
		super(new HttpResponse(statusCode, true, toJSON(errorCode, errorMessage)));
	}
	
	public static String toJSON(ErrorCode errorCode, String errorMessage) {
		var obj = new HashMap<String, Object>();
		var error = new HashMap<String, Object>();
		error.put("code", errorCode.name());
		error.put("message", errorMessage);
		var errors = new Object[] {error};
		obj.put("errors", errors);
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
