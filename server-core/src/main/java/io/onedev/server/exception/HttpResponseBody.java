package io.onedev.server.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

public class HttpResponseBody {

	private final boolean json;

	private final String text;

	public HttpResponseBody(boolean json, String text) {
		this.json = json;
		this.text = text;
	}
	
	public HttpResponseBody(Map<String, Object> jsonValue) {
		this(true, getJsonText(jsonValue));
	}
	
	private static String getJsonText(Map<String, Object> jsonValue) {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(jsonValue);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isJson() {
		return json;
	}

	public String getText() {
		return text;
	}

	public String getContentType() {
		return json? APPLICATION_JSON: TEXT_PLAIN;
	}
	
}
