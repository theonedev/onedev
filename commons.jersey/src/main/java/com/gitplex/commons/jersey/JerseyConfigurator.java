package com.gitplex.commons.jersey;

import org.glassfish.jersey.server.ResourceConfig;

public interface JerseyConfigurator {
	void configure(ResourceConfig resourceConfig);
}
