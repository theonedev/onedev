package com.pmease.commons.hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.jackson.ObjectMapperConfigurator;

@Singleton
public class HibernateObjectMapperConfigurator implements ObjectMapperConfigurator {

	private final HibernateJacksonModule module; 
	
	@Inject
	public HibernateObjectMapperConfigurator(HibernateJacksonModule module) {
		this.module = module;
	}

	@Override
	public void configure(ObjectMapper objectMapper) {
		objectMapper.registerModule(module);
	}
	
}
