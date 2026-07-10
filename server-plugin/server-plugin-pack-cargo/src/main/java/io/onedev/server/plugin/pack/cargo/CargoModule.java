package io.onedev.server.plugin.pack.cargo;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CargoModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(CargoPackHandler.class);
		contribute(PackHandler.class, CargoPackHandler.class);
		contribute(PackSupport.class, new CargoPackSupport());
	}
}
