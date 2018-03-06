package io.onedev.server.util.jackson.hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.util.jackson.ObjectMapperConfigurator;

@Singleton
public class HibernateObjectMapperConfigurator implements ObjectMapperConfigurator {

	private final HibernateObjectMapperModule module; 
	
	@Inject
	public HibernateObjectMapperConfigurator(HibernateObjectMapperModule module) {
		this.module = module;
	}

	@Override
	public void configure(ObjectMapper objectMapper) {
		objectMapper.registerModule(module);
	}
	
}
