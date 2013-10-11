package com.pmease.commons.jersey;

import com.pmease.commons.loader.AbstractPluginModule;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class JerseyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(JerseyEnvironment app) {
			}
		});
		
		bind(JerseyEnvironment.class);
		
		bind(ResourceConfig.class).toProvider(JerseyEnvironment.class);
	}

}
