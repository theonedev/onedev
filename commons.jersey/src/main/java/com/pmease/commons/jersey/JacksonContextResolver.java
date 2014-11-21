package com.pmease.commons.jersey;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.jackson.ExternalView;

@Provider
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {

	private final ObjectMapper objectMapper;
	
	@Inject
	public JacksonContextResolver(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper.copy();
		this.objectMapper.setConfig(this.objectMapper.getSerializationConfig().withView(ExternalView.class));
	}
	
	@Override
	public ObjectMapper getContext(Class<?> type) {
		return objectMapper;
	}

}
