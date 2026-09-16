package io.onedev.server.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
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
