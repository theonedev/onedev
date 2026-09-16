package io.onedev.server.rest;

import jakarta.inject.Provider;

import org.glassfish.jersey.server.ResourceConfig;

public class ResourceConfigProvider implements Provider<ResourceConfig> {

	@Override
	public ResourceConfig get() {
		return ResourceConfig.forApplicationClass(JerseyApplication.class);
	}

}
