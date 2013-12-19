package com.pmease.commons.jackson;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

public class JsOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> map = new LinkedHashMap<>();

	@Override
	public String toString() {
		ObjectMapper objectMapper = AppLoader.getInstance(ObjectMapper.class);
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public JsOptions add(String key, Object value) {
		map.put(key, value);
		return this;
	}
	
	public JsOptions remove(String key) {
		map.remove(key);
		return this;
	}

}
