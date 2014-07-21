package com.pmease.gitplex.rest;

import org.glassfish.jersey.server.ResourceConfig;

import com.pmease.commons.jersey.JerseyConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.gitplex.rest.resource.ResourceLocator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RestModule extends AbstractPluginModule {

	public static final String SERVLET_PATH = "/rest";
	
	@Override
	protected void configure() {
		super.configure();
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.packages(true, ResourceLocator.class.getPackage().getName());
			}
			
		});
		
	}

}
