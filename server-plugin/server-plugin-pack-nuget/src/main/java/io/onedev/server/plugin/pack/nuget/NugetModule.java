package io.onedev.server.plugin.pack.nuget;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackService;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class NugetModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(NugetPackService.class);
		contribute(PackService.class, NugetPackService.class);
		contribute(PackSupport.class, new NugetPackSupport());
	}

}
