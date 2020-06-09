package io.onedev.server.plugin.buildspec.gradle;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.script.ScriptContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GradleModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, GradleJobSuggestion.class);
		
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName(GradleJobSuggestion.DETERMINE_DOCKER_IMAGE);
				script.setContent(Lists.newArrayList("io.onedev.server.plugin.buildspec.gradle.GradleJobSuggestion.determineDockerImage()"));
				return script;
			}
			
		});
		
	}

}
