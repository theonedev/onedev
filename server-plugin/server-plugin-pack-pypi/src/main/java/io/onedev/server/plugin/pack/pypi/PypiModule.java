package io.onedev.server.plugin.pack.pypi;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.pack.PackHandler;
import io.onedev.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class PypiModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(PypiPackHandler.class);
		contribute(PackHandler.class, PypiPackHandler.class);
		contribute(PackSupport.class, new PypiPackSupport());
	}

}
