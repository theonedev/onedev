package com.pmease.commons.jersey;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonContextResolver implements ContextResolver<ObjectMapper> {

	private final ObjectMapper objectMapper;
	
	@Inject
	public JacksonContextResolver(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	public ObjectMapper getContext(Class<?> type) {
		return objectMapper;
	}

}
