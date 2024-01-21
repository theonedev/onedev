package io.onedev.server.plugin.pack.maven;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackService;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MavenModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(MavenPackService.class);
		contribute(PackService.class, MavenPackService.class);
		contribute(PackSupport.class, new MavenPackSupport());
	}

}
