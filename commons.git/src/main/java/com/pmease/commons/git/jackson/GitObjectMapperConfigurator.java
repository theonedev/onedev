package com.pmease.commons.git.jackson;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.jackson.ObjectMapperConfigurator;

@Singleton
public class GitObjectMapperConfigurator implements ObjectMapperConfigurator {

	private final GitObjectMapperModule module; 
	
	@Inject
	public GitObjectMapperConfigurator(GitObjectMapperModule module) {
		this.module = module;
	}

	@Override
	public void configure(ObjectMapper objectMapper) {
		objectMapper.registerModule(module);
	}
	
}
