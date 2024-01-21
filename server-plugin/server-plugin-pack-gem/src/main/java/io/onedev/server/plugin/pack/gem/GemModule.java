package io.onedev.server.plugin.pack.gem;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackService;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class GemModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(GemPackService.class);
		contribute(PackService.class, GemPackService.class);
		contribute(PackSupport.class, new GemPackSupport());
	}

}
