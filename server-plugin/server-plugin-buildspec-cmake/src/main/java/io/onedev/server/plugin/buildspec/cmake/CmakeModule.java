package io.onedev.server.plugin.buildspec.cmake;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.buildspec.job.JobSuggestion;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CmakeModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, CmakeJobSuggestion.class);
	}

}
