package com.pmease.commons.jackson;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@Singleton
public class ObjectMapperProvider implements Provider<ObjectMapper> {

	private final Set<ObjectMapperConfigurator> configurators;
	
	@Inject
	public ObjectMapperProvider(Set<ObjectMapperConfigurator> configurators) {
		this.configurators = configurators;
	}
	
	@Override
	public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new JodaModule());
		
//        mapper.enableDefaultTyping();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));		
		
		for (ObjectMapperConfigurator each: configurators)
			each.configure(mapper);
		
		return mapper;
	}

}
