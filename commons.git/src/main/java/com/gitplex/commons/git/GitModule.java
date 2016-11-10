package com.gitplex.commons.git;

import com.gitplex.commons.git.jackson.GitObjectMapperConfigurator;
import com.gitplex.commons.jackson.ObjectMapperConfigurator;
import com.gitplex.commons.loader.AbstractPluginModule;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GitModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ObjectMapperConfigurator.class, GitObjectMapperConfigurator.class);
	}

}
