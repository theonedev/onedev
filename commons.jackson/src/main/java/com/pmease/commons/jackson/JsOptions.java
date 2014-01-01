package com.pmease.commons.jackson;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;

public class JsOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Serializable> map = new LinkedHashMap<>();

	@Override
	public String toString() {
		ObjectMapper objectMapper = AppLoader.getInstance(ObjectMapper.class);
		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public JsOptions put(String key, @Nullable Serializable value) {
		Preconditions.checkNotNull(key, "key");
		if (value != null) {
			map.put(key, value);
		} else {
			map.remove(key);
		}
		
		return this;
	}

	public Serializable get(String key) {
		Preconditions.checkNotNull(key, "key");
		return map.get(key);
	}
	
	public JsOptions remove(String key) {
		map.remove(key);
		return this;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}
}
