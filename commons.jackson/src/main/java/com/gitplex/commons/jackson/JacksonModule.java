package com.gitplex.commons.jackson;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class JacksonModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);

		// Add a dummy contribution to avoid error if no other contributions
		contribute(ObjectMapperConfigurator.class, new ObjectMapperConfigurator() {
			
			@Override
			public void configure(ObjectMapper objectMapper) {
			}

		});
	}

}
