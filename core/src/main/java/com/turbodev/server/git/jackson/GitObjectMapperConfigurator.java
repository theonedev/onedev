package com.turbodev.server.git.jackson;

import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.turbodev.server.util.jackson.ObjectMapperConfigurator;

@Singleton
public class GitObjectMapperConfigurator implements ObjectMapperConfigurator {

	@Override
	public void configure(ObjectMapper objectMapper) {
		SimpleModule module = new SimpleModule("GitModule");
		module.addSerializer(ObjectId.class, new ObjectIdSerializer());
		module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
		objectMapper.registerModule(module);
	}
	
}
