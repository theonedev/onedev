package io.onedev.server.util.jackson.git;

import jakarta.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.onedev.server.util.jackson.ObjectMapperConfigurator;

@Singleton
public class GitObjectMapperConfigurator implements ObjectMapperConfigurator {

	@Override
	public void configure(ObjectMapper objectMapper) {
		SimpleModule module = new SimpleModule("GitModule");
		module.addSerializer(ObjectId.class, new ObjectIdSerializer());
		module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
		module.addSerializer(PersonIdent.class, new PersonIdentSerializer());
		objectMapper.registerModule(module);
	}
	
}
