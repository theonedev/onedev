package com.gitplex.commons.hibernate.jackson;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.commons.jackson.ObjectMapperConfigurator;

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
