package io.onedev.server.plugin.maven;

import io.onedev.commons.launcher.loader.AbstractPluginModule;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.log.LogNormalizer;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MavenModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(DefaultCISpecProvider.class, DefaultMavenCISpecProvider.class);
		contribute(LogNormalizer.class, MavenLogNormalizer.class);
	}

}
