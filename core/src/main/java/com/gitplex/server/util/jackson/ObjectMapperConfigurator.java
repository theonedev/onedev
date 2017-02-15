package com.gitplex.server.util.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperConfigurator {
	void configure(ObjectMapper objectMapper);
}
