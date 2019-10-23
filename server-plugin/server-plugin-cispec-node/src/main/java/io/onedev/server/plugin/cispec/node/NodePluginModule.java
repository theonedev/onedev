package io.onedev.server.plugin.cispec.node;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.ci.job.JobSuggestion;
import io.onedev.server.ci.job.NamedFunction;
import io.onedev.server.model.Build;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class NodePluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, NodeJobSuggestion.class);
		
		contribute(NamedFunction.class, new NamedFunction() {

			@Override
			public String getName() {
				return NodeJobSuggestion.DETERMINE_PROJECT_VERSION;
			}

			@Override
			public String call(Build build) {
				return NodeJobSuggestion.determineProjectVersion(build);
			}
			
		});
	}

}
