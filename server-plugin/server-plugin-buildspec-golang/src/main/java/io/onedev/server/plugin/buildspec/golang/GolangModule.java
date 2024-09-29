package io.onedev.server.plugin.buildspec.golang;

import com.google.common.collect.Lists;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.ScriptContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GolangModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, GolangJobSuggestion.class);

		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName(GolangJobSuggestion.DETERMINE_GO_VERSION);
				script.setContent(Lists.newArrayList("io.onedev.server.plugin.buildspec.golang.GolangJobSuggestion.determineGoVersion()"));
				return script;
			}

		});
	}

}
