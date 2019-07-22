package io.onedev.server.util.jackson;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.launcher.loader.AppLoader;

@SuppressWarnings("serial")
public class JsonOptions extends LinkedHashMap<String, String> {
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = AppLoader.getInstance(ObjectMapper.class);
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
