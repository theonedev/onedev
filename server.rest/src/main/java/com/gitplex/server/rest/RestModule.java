package com.gitplex.server.rest;

import org.glassfish.jersey.server.ResourceConfig;

import com.gitplex.commons.jersey.JerseyConfigurator;
import com.gitplex.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RestModule extends AbstractPluginModule {
	
	@Override
	protected void configure() {
		super.configure();
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.packages(RestModule.class.getPackage().getName());
			}
			
		});
		
	}

}
