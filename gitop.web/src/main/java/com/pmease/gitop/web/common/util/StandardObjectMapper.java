package com.pmease.gitop.web.common.util;

import com.codahale.dropwizard.jackson.AnnotationSensitivePropertyNamingStrategy;
import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import com.codahale.dropwizard.jackson.FuzzyEnumModule;
import com.codahale.dropwizard.jackson.GuavaExtrasModule;
import com.codahale.dropwizard.jackson.LogbackModule;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public class StandardObjectMapper {
	private static final ObjectMapper MAPPER = newObjectMapper();

	public static ObjectMapper getInstance() {
		return MAPPER;
	}

	/**
	 * Creates a new {@link ObjectMapper} which is compatible with DropWizard
	 */
	private static ObjectMapper newObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());
		mapper.registerModule(new LogbackModule());
		mapper.registerModule(new GuavaExtrasModule());
		mapper.registerModule(new JodaModule());
		mapper.registerModule(new AfterburnerModule());
		mapper.registerModule(new FuzzyEnumModule());
		mapper.setPropertyNamingStrategy(new AnnotationSensitivePropertyNamingStrategy());
		mapper.setSubtypeResolver(new DiscoverableSubtypeResolver());
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
		mapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
		// to allow C/C++ style comments in JSON (non-standard, disabled by
		// default)
		JsonFactory jsonFactory = mapper.getFactory();
		jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
		// to allow (non-standard) unquoted field names in JSON:
		jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
		// to allow use of apostrophes (single quotes), non standard
		jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
		jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
		jsonFactory
				.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
		return mapper;
	}

	private StandardObjectMapper() {
		throw new AssertionError();
	}
}
