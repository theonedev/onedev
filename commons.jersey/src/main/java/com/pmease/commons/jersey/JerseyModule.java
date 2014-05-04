package com.pmease.commons.jersey;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ResourceConfig;

import com.pmease.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class JerseyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
	}

}
