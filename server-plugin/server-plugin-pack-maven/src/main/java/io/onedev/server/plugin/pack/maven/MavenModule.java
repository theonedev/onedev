package io.onedev.server.plugin.pack.maven;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MavenModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(MavenPackHandler.class);
		contribute(PackHandler.class, MavenPackHandler.class);
		contribute(PackSupport.class, new MavenPackSupport());
	}

}
